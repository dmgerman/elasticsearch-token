begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.alias
package|package
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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchGenerationException
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|StreamOutput
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
name|Streamable
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
name|XContentType
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Represents an alias, to be associated with an index  */
end_comment

begin_class
DECL|class|Alias
specifier|public
class|class
name|Alias
implements|implements
name|Streamable
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
annotation|@
name|Nullable
DECL|field|filter
specifier|private
name|String
name|filter
decl_stmt|;
annotation|@
name|Nullable
DECL|field|indexRouting
specifier|private
name|String
name|indexRouting
decl_stmt|;
annotation|@
name|Nullable
DECL|field|searchRouting
specifier|private
name|String
name|searchRouting
decl_stmt|;
DECL|method|Alias
specifier|private
name|Alias
parameter_list|()
block|{      }
DECL|method|Alias
specifier|public
name|Alias
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * Returns the alias name      */
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
comment|/**      * Returns the filter associated with the alias      */
DECL|method|filter
specifier|public
name|String
name|filter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
comment|/**      * Associates a filter to the alias      */
DECL|method|filter
specifier|public
name|Alias
name|filter
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Associates a filter to the alias      */
DECL|method|filter
specifier|public
name|Alias
name|filter
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
parameter_list|)
block|{
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
name|filter
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|filter
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|filter
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Associates a filter to the alias      */
DECL|method|filter
specifier|public
name|Alias
name|filter
parameter_list|(
name|QueryBuilder
name|filterBuilder
parameter_list|)
block|{
if|if
condition|(
name|filterBuilder
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|filter
operator|=
literal|null
expr_stmt|;
return|return
name|this
return|;
block|}
try|try
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
name|this
operator|.
name|filter
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to build json for alias request"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Associates a routing value to the alias      */
DECL|method|routing
specifier|public
name|Alias
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|indexRouting
operator|=
name|routing
expr_stmt|;
name|this
operator|.
name|searchRouting
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the index routing value associated with the alias      */
DECL|method|indexRouting
specifier|public
name|String
name|indexRouting
parameter_list|()
block|{
return|return
name|indexRouting
return|;
block|}
comment|/**      * Associates an index routing value to the alias      */
DECL|method|indexRouting
specifier|public
name|Alias
name|indexRouting
parameter_list|(
name|String
name|indexRouting
parameter_list|)
block|{
name|this
operator|.
name|indexRouting
operator|=
name|indexRouting
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the search routing value associated with the alias      */
DECL|method|searchRouting
specifier|public
name|String
name|searchRouting
parameter_list|()
block|{
return|return
name|searchRouting
return|;
block|}
comment|/**      * Associates a search routing value to the alias      */
DECL|method|searchRouting
specifier|public
name|Alias
name|searchRouting
parameter_list|(
name|String
name|searchRouting
parameter_list|)
block|{
name|this
operator|.
name|searchRouting
operator|=
name|searchRouting
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to read an alias from the provided input stream      */
DECL|method|read
specifier|public
specifier|static
name|Alias
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Alias
name|alias
init|=
operator|new
name|Alias
argument_list|()
decl_stmt|;
name|alias
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|alias
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|filter
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|indexRouting
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|searchRouting
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexRouting
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|searchRouting
argument_list|)
expr_stmt|;
block|}
comment|/**      * Parses an alias and returns its parsed representation      */
DECL|method|fromXContent
specifier|public
specifier|static
name|Alias
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|Alias
name|alias
init|=
operator|new
name|Alias
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
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
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No alias is specified"
argument_list|)
throw|;
block|}
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"filter"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
init|=
name|parser
operator|.
name|mapOrdered
argument_list|()
decl_stmt|;
name|alias
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|alias
operator|.
name|routing
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"index_routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"indexRouting"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"index-routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|alias
operator|.
name|indexRouting
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"search_routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"searchRouting"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"search-routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|alias
operator|.
name|searchRouting
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|alias
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
name|Alias
name|alias
init|=
operator|(
name|Alias
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|?
operator|!
name|name
operator|.
name|equals
argument_list|(
name|alias
operator|.
name|name
argument_list|)
else|:
name|alias
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|name
operator|!=
literal|null
condition|?
name|name
operator|.
name|hashCode
argument_list|()
else|:
literal|0
return|;
block|}
block|}
end_class

end_unit

