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
comment|/**  *  */
end_comment

begin_class
DECL|class|AliasAction
specifier|public
class|class
name|AliasAction
implements|implements
name|Streamable
block|{
DECL|enum|Type
specifier|public
specifier|static
enum|enum
name|Type
block|{
DECL|enum constant|ADD
name|ADD
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
DECL|enum constant|REMOVE
name|REMOVE
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|;
DECL|field|value
specifier|private
specifier|final
name|byte
name|value
decl_stmt|;
DECL|method|Type
name|Type
parameter_list|(
name|byte
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|value
specifier|public
name|byte
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
DECL|method|fromValue
specifier|public
specifier|static
name|Type
name|fromValue
parameter_list|(
name|byte
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|0
condition|)
block|{
return|return
name|ADD
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|==
literal|1
condition|)
block|{
return|return
name|REMOVE
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No type for action ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|field|actionType
specifier|private
name|Type
name|actionType
decl_stmt|;
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|alias
specifier|private
name|String
name|alias
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
DECL|method|AliasAction
specifier|private
name|AliasAction
parameter_list|()
block|{      }
DECL|method|AliasAction
specifier|public
name|AliasAction
parameter_list|(
name|AliasAction
name|other
parameter_list|)
block|{
name|this
operator|.
name|actionType
operator|=
name|other
operator|.
name|actionType
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|other
operator|.
name|index
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|other
operator|.
name|alias
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|other
operator|.
name|filter
expr_stmt|;
name|this
operator|.
name|indexRouting
operator|=
name|other
operator|.
name|indexRouting
expr_stmt|;
name|this
operator|.
name|searchRouting
operator|=
name|other
operator|.
name|searchRouting
expr_stmt|;
block|}
DECL|method|AliasAction
specifier|public
name|AliasAction
parameter_list|(
name|Type
name|actionType
parameter_list|)
block|{
name|this
operator|.
name|actionType
operator|=
name|actionType
expr_stmt|;
block|}
DECL|method|AliasAction
specifier|public
name|AliasAction
parameter_list|(
name|Type
name|actionType
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|this
operator|.
name|actionType
operator|=
name|actionType
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
block|}
DECL|method|AliasAction
specifier|public
name|AliasAction
parameter_list|(
name|Type
name|actionType
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|,
name|String
name|filter
parameter_list|)
block|{
name|this
operator|.
name|actionType
operator|=
name|actionType
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
DECL|method|actionType
specifier|public
name|Type
name|actionType
parameter_list|()
block|{
return|return
name|actionType
return|;
block|}
DECL|method|index
specifier|public
name|AliasAction
name|index
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|alias
specifier|public
name|AliasAction
name|alias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|alias
specifier|public
name|String
name|alias
parameter_list|()
block|{
return|return
name|alias
return|;
block|}
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
DECL|method|filter
specifier|public
name|AliasAction
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
DECL|method|filter
specifier|public
name|AliasAction
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
DECL|method|filter
specifier|public
name|AliasAction
name|filter
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
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
name|queryBuilder
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
DECL|method|routing
specifier|public
name|AliasAction
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
DECL|method|indexRouting
specifier|public
name|AliasAction
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
DECL|method|searchRouting
specifier|public
name|AliasAction
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
DECL|method|readAliasAction
specifier|public
specifier|static
name|AliasAction
name|readAliasAction
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|AliasAction
name|aliasAction
init|=
operator|new
name|AliasAction
argument_list|()
decl_stmt|;
name|aliasAction
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|aliasAction
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
name|actionType
operator|=
name|Type
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|alias
operator|=
name|in
operator|.
name|readOptionalString
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
name|writeByte
argument_list|(
name|actionType
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|alias
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
DECL|method|newAddAliasAction
specifier|public
specifier|static
name|AliasAction
name|newAddAliasAction
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
operator|new
name|AliasAction
argument_list|(
name|Type
operator|.
name|ADD
argument_list|,
name|index
argument_list|,
name|alias
argument_list|)
return|;
block|}
DECL|method|newRemoveAliasAction
specifier|public
specifier|static
name|AliasAction
name|newRemoveAliasAction
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
operator|new
name|AliasAction
argument_list|(
name|Type
operator|.
name|REMOVE
argument_list|,
name|index
argument_list|,
name|alias
argument_list|)
return|;
block|}
block|}
end_class

end_unit

