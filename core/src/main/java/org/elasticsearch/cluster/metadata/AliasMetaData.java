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
name|cluster
operator|.
name|AbstractDiffable
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
name|Collections
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AliasMetaData
specifier|public
class|class
name|AliasMetaData
extends|extends
name|AbstractDiffable
argument_list|<
name|AliasMetaData
argument_list|>
block|{
DECL|field|PROTO
specifier|public
specifier|static
specifier|final
name|AliasMetaData
name|PROTO
init|=
operator|new
name|AliasMetaData
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|field|alias
specifier|private
specifier|final
name|String
name|alias
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|CompressedXContent
name|filter
decl_stmt|;
DECL|field|indexRouting
specifier|private
specifier|final
name|String
name|indexRouting
decl_stmt|;
DECL|field|searchRouting
specifier|private
specifier|final
name|String
name|searchRouting
decl_stmt|;
DECL|field|searchRoutingValues
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|searchRoutingValues
decl_stmt|;
DECL|method|AliasMetaData
specifier|private
name|AliasMetaData
parameter_list|(
name|String
name|alias
parameter_list|,
name|CompressedXContent
name|filter
parameter_list|,
name|String
name|indexRouting
parameter_list|,
name|String
name|searchRouting
parameter_list|)
block|{
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
name|this
operator|.
name|indexRouting
operator|=
name|indexRouting
expr_stmt|;
name|this
operator|.
name|searchRouting
operator|=
name|searchRouting
expr_stmt|;
if|if
condition|(
name|searchRouting
operator|!=
literal|null
condition|)
block|{
name|searchRoutingValues
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|searchRouting
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|searchRoutingValues
operator|=
name|emptySet
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|AliasMetaData
specifier|private
name|AliasMetaData
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|this
argument_list|(
name|alias
argument_list|,
name|aliasMetaData
operator|.
name|filter
argument_list|()
argument_list|,
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
argument_list|,
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
argument_list|)
expr_stmt|;
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
DECL|method|getAlias
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|alias
argument_list|()
return|;
block|}
DECL|method|filter
specifier|public
name|CompressedXContent
name|filter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
DECL|method|getFilter
specifier|public
name|CompressedXContent
name|getFilter
parameter_list|()
block|{
return|return
name|filter
argument_list|()
return|;
block|}
DECL|method|filteringRequired
specifier|public
name|boolean
name|filteringRequired
parameter_list|()
block|{
return|return
name|filter
operator|!=
literal|null
return|;
block|}
DECL|method|getSearchRouting
specifier|public
name|String
name|getSearchRouting
parameter_list|()
block|{
return|return
name|searchRouting
argument_list|()
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
DECL|method|getIndexRouting
specifier|public
name|String
name|getIndexRouting
parameter_list|()
block|{
return|return
name|indexRouting
argument_list|()
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
DECL|method|searchRoutingValues
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|searchRoutingValues
parameter_list|()
block|{
return|return
name|searchRoutingValues
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|alias
argument_list|)
return|;
block|}
DECL|method|newAliasMetaDataBuilder
specifier|public
specifier|static
name|Builder
name|newAliasMetaDataBuilder
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|alias
argument_list|)
return|;
block|}
comment|/**      * Creates a new AliasMetaData instance with same content as the given one, but with a different alias name      */
DECL|method|newAliasMetaData
specifier|public
specifier|static
name|AliasMetaData
name|newAliasMetaData
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|,
name|String
name|newAlias
parameter_list|)
block|{
return|return
operator|new
name|AliasMetaData
argument_list|(
name|aliasMetaData
argument_list|,
name|newAlias
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
name|AliasMetaData
name|that
init|=
operator|(
name|AliasMetaData
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|alias
operator|!=
literal|null
condition|?
operator|!
name|alias
operator|.
name|equals
argument_list|(
name|that
operator|.
name|alias
argument_list|)
else|:
name|that
operator|.
name|alias
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|?
operator|!
name|filter
operator|.
name|equals
argument_list|(
name|that
operator|.
name|filter
argument_list|)
else|:
name|that
operator|.
name|filter
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|indexRouting
operator|!=
literal|null
condition|?
operator|!
name|indexRouting
operator|.
name|equals
argument_list|(
name|that
operator|.
name|indexRouting
argument_list|)
else|:
name|that
operator|.
name|indexRouting
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|searchRouting
operator|!=
literal|null
condition|?
operator|!
name|searchRouting
operator|.
name|equals
argument_list|(
name|that
operator|.
name|searchRouting
argument_list|)
else|:
name|that
operator|.
name|searchRouting
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
name|int
name|result
init|=
name|alias
operator|!=
literal|null
condition|?
name|alias
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
operator|(
name|filter
operator|!=
literal|null
condition|?
name|filter
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|indexRouting
operator|!=
literal|null
condition|?
name|indexRouting
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|searchRouting
operator|!=
literal|null
condition|?
name|searchRouting
operator|.
name|hashCode
argument_list|()
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
name|alias
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|filter
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|filter
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexRouting
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexRouting
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchRouting
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|searchRouting
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|AliasMetaData
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|alias
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|CompressedXContent
name|filter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|filter
operator|=
name|CompressedXContent
operator|.
name|readCompressedString
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|String
name|indexRouting
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|indexRouting
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|String
name|searchRouting
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|searchRouting
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|AliasMetaData
argument_list|(
name|alias
argument_list|,
name|filter
argument_list|,
name|indexRouting
argument_list|,
name|searchRouting
argument_list|)
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|alias
specifier|private
specifier|final
name|String
name|alias
decl_stmt|;
DECL|field|filter
specifier|private
name|CompressedXContent
name|filter
decl_stmt|;
DECL|field|indexRouting
specifier|private
name|String
name|indexRouting
decl_stmt|;
DECL|field|searchRouting
specifier|private
name|String
name|searchRouting
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
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
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|)
block|{
name|this
argument_list|(
name|aliasMetaData
operator|.
name|alias
argument_list|()
argument_list|)
expr_stmt|;
name|filter
operator|=
name|aliasMetaData
operator|.
name|filter
argument_list|()
expr_stmt|;
name|indexRouting
operator|=
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
expr_stmt|;
name|searchRouting
operator|=
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
expr_stmt|;
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
name|Builder
name|filter
parameter_list|(
name|CompressedXContent
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
name|Builder
name|filter
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasLength
argument_list|(
name|filter
argument_list|)
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
name|filter
argument_list|)
init|)
block|{
name|filter
argument_list|(
name|parser
operator|.
name|mapOrdered
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|Builder
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
name|jsonBuilder
argument_list|()
operator|.
name|map
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|this
operator|.
name|filter
operator|=
operator|new
name|CompressedXContent
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
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
DECL|method|filter
specifier|public
name|Builder
name|filter
parameter_list|(
name|XContentBuilder
name|filterBuilder
parameter_list|)
block|{
try|try
block|{
return|return
name|filter
argument_list|(
name|filterBuilder
operator|.
name|string
argument_list|()
argument_list|)
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
name|Builder
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
name|Builder
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
name|Builder
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
DECL|method|build
specifier|public
name|AliasMetaData
name|build
parameter_list|()
block|{
return|return
operator|new
name|AliasMetaData
argument_list|(
name|alias
argument_list|,
name|filter
argument_list|,
name|indexRouting
argument_list|,
name|searchRouting
argument_list|)
return|;
block|}
DECL|method|toXContent
specifier|public
specifier|static
name|void
name|toXContent
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|aliasMetaData
operator|.
name|alias
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|boolean
name|binary
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"binary"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliasMetaData
operator|.
name|filter
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|binary
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|,
name|aliasMetaData
operator|.
name|filter
operator|.
name|compressed
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|data
init|=
name|aliasMetaData
operator|.
name|filter
argument_list|()
operator|.
name|uncompressed
argument_list|()
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|data
argument_list|)
operator|.
name|createParser
argument_list|(
name|data
argument_list|)
init|)
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
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index_routing"
argument_list|,
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"search_routing"
argument_list|,
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|AliasMetaData
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
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
comment|// no data...
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
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
name|builder
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
name|VALUE_EMBEDDED_OBJECT
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
name|builder
operator|.
name|filter
argument_list|(
operator|new
name|CompressedXContent
argument_list|(
name|parser
operator|.
name|binaryValue
argument_list|()
argument_list|)
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
name|builder
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
condition|)
block|{
name|builder
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
condition|)
block|{
name|builder
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
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|aliasMetaData
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|AliasMetaData
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PROTO
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

