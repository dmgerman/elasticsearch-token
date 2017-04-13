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
name|XContentParser
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
operator|.
name|Token
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
name|json
operator|.
name|JsonXContent
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
name|CompletionFieldMapper
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
name|query
operator|.
name|QueryParseContext
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
name|List
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

begin_comment
comment|/**  * A {@link ContextMapping} defines criteria that can be used to  * filter and/or boost suggestions at query time for {@link CompletionFieldMapper}.  *  * Implementations have to define how contexts are parsed at query/index time  */
end_comment

begin_class
DECL|class|ContextMapping
specifier|public
specifier|abstract
class|class
name|ContextMapping
parameter_list|<
name|T
extends|extends
name|ToXContent
parameter_list|>
implements|implements
name|ToXContent
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_TYPE
init|=
literal|"type"
decl_stmt|;
DECL|field|FIELD_NAME
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_NAME
init|=
literal|"name"
decl_stmt|;
DECL|field|type
specifier|protected
specifier|final
name|Type
name|type
decl_stmt|;
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|enum|Type
specifier|public
enum|enum
name|Type
block|{
DECL|enum constant|CATEGORY
DECL|enum constant|GEO
name|CATEGORY
block|,
name|GEO
block|;
DECL|method|fromString
specifier|public
specifier|static
name|Type
name|fromString
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"category"
argument_list|)
condition|)
block|{
return|return
name|CATEGORY
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"geo"
argument_list|)
condition|)
block|{
return|return
name|GEO
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No context type for ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * Define a new context mapping of a specific type      *      * @param type type of context mapping, either {@link Type#CATEGORY} or {@link Type#GEO}      * @param name name of context mapping      */
DECL|method|ContextMapping
specifier|protected
name|ContextMapping
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * @return the type name of the context      */
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * @return the name/id of the context      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**      * Parses a set of index-time contexts.      */
DECL|method|parseContext
specifier|public
specifier|abstract
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|parseContext
parameter_list|(
name|ParseContext
name|parseContext
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
throws|,
name|ElasticsearchParseException
function_decl|;
comment|/**      * Retrieves a set of context from a<code>document</code> at index-time.      */
DECL|method|parseContext
specifier|protected
specifier|abstract
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|parseContext
parameter_list|(
name|ParseContext
operator|.
name|Document
name|document
parameter_list|)
function_decl|;
comment|/**      * Prototype for the query context      */
DECL|method|fromXContent
specifier|protected
specifier|abstract
name|T
name|fromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Parses query contexts for this mapper      */
DECL|method|parseQueryContext
specifier|public
specifier|final
name|List
argument_list|<
name|InternalQueryContext
argument_list|>
name|parseQueryContext
parameter_list|(
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|ElasticsearchParseException
block|{
name|List
argument_list|<
name|T
argument_list|>
name|queryContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|queryContexts
operator|.
name|add
argument_list|(
name|fromXContent
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|queryContexts
operator|.
name|add
argument_list|(
name|fromXContent
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|toInternalQueryContexts
argument_list|(
name|queryContexts
argument_list|)
return|;
block|}
comment|/**      * Convert query contexts to common representation      */
DECL|method|toInternalQueryContexts
specifier|protected
specifier|abstract
name|List
argument_list|<
name|InternalQueryContext
argument_list|>
name|toInternalQueryContexts
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|queryContexts
parameter_list|)
function_decl|;
comment|/**      * Implementations should add specific configurations      * that need to be persisted      */
DECL|method|toInnerXContent
specifier|protected
specifier|abstract
name|XContentBuilder
name|toInnerXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|toXContent
specifier|public
specifier|final
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
name|builder
operator|.
name|field
argument_list|(
name|FIELD_NAME
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FIELD_TYPE
argument_list|,
name|type
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|toInnerXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|ContextMapping
name|that
init|=
operator|(
name|ContextMapping
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|type
operator|!=
name|that
operator|.
name|type
condition|)
return|return
literal|false
return|;
return|return
name|name
operator|.
name|equals
argument_list|(
name|that
operator|.
name|name
argument_list|)
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
name|type
argument_list|,
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
return|return
name|toXContent
argument_list|(
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
DECL|class|InternalQueryContext
specifier|public
specifier|static
class|class
name|InternalQueryContext
block|{
DECL|field|context
specifier|public
specifier|final
name|String
name|context
decl_stmt|;
DECL|field|boost
specifier|public
specifier|final
name|int
name|boost
decl_stmt|;
DECL|field|isPrefix
specifier|public
specifier|final
name|boolean
name|isPrefix
decl_stmt|;
DECL|method|InternalQueryContext
specifier|public
name|InternalQueryContext
parameter_list|(
name|String
name|context
parameter_list|,
name|int
name|boost
parameter_list|,
name|boolean
name|isPrefix
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
name|this
operator|.
name|isPrefix
operator|=
name|isPrefix
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|InternalQueryContext
name|that
init|=
operator|(
name|InternalQueryContext
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|boost
operator|!=
name|that
operator|.
name|boost
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|isPrefix
operator|!=
name|that
operator|.
name|isPrefix
condition|)
return|return
literal|false
return|;
return|return
name|context
operator|!=
literal|null
condition|?
name|context
operator|.
name|equals
argument_list|(
name|that
operator|.
name|context
argument_list|)
else|:
name|that
operator|.
name|context
operator|==
literal|null
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
name|int
name|result
init|=
name|context
operator|!=
literal|null
condition|?
name|context
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|boost
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|isPrefix
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"QueryContext{"
operator|+
literal|"context='"
operator|+
name|context
operator|+
literal|'\''
operator|+
literal|", boost="
operator|+
name|boost
operator|+
literal|", isPrefix="
operator|+
name|isPrefix
operator|+
literal|'}'
return|;
block|}
block|}
block|}
end_class

end_unit

