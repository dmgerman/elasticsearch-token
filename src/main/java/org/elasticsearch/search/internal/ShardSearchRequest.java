begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
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
name|action
operator|.
name|search
operator|.
name|SearchRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|routing
operator|.
name|ShardRouting
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
name|search
operator|.
name|Scroll
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequest
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
operator|.
name|readScroll
import|;
end_import

begin_comment
comment|/**  * Source structure:  *<p/>  *<pre>  * {  *  from : 0, size : 20, (optional, can be set on the request)  *  sort : { "name.first" : {}, "name.last" : { reverse : true } }  *  fields : [ "name.first", "name.last" ]  *  query : { ... }  *  facets : {  *      "facet1" : {  *          query : { ... }  *      }  *  }  * }  *</pre>  */
end_comment

begin_class
DECL|class|ShardSearchRequest
specifier|public
class|class
name|ShardSearchRequest
extends|extends
name|TransportRequest
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|shardId
specifier|private
name|int
name|shardId
decl_stmt|;
DECL|field|numberOfShards
specifier|private
name|int
name|numberOfShards
decl_stmt|;
DECL|field|searchType
specifier|private
name|SearchType
name|searchType
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|filteringAliases
specifier|private
name|String
index|[]
name|filteringAliases
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|extraSource
specifier|private
name|BytesReference
name|extraSource
decl_stmt|;
DECL|field|templateSource
specifier|private
name|BytesReference
name|templateSource
decl_stmt|;
DECL|field|templateName
specifier|private
name|String
name|templateName
decl_stmt|;
DECL|field|templateParams
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
decl_stmt|;
DECL|field|nowInMillis
specifier|private
name|long
name|nowInMillis
decl_stmt|;
DECL|method|ShardSearchRequest
specifier|public
name|ShardSearchRequest
parameter_list|()
block|{     }
DECL|method|ShardSearchRequest
specifier|public
name|ShardSearchRequest
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|,
name|ShardRouting
name|shardRouting
parameter_list|,
name|int
name|numberOfShards
parameter_list|)
block|{
name|super
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|shardRouting
operator|.
name|index
argument_list|()
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardRouting
operator|.
name|id
argument_list|()
expr_stmt|;
name|this
operator|.
name|numberOfShards
operator|=
name|numberOfShards
expr_stmt|;
name|this
operator|.
name|searchType
operator|=
name|searchRequest
operator|.
name|searchType
argument_list|()
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|searchRequest
operator|.
name|source
argument_list|()
expr_stmt|;
name|this
operator|.
name|extraSource
operator|=
name|searchRequest
operator|.
name|extraSource
argument_list|()
expr_stmt|;
name|this
operator|.
name|templateSource
operator|=
name|searchRequest
operator|.
name|templateSource
argument_list|()
expr_stmt|;
name|this
operator|.
name|templateName
operator|=
name|searchRequest
operator|.
name|templateName
argument_list|()
expr_stmt|;
name|this
operator|.
name|templateParams
operator|=
name|searchRequest
operator|.
name|templateParams
argument_list|()
expr_stmt|;
name|this
operator|.
name|scroll
operator|=
name|searchRequest
operator|.
name|scroll
argument_list|()
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|searchRequest
operator|.
name|types
argument_list|()
expr_stmt|;
block|}
DECL|method|ShardSearchRequest
specifier|public
name|ShardSearchRequest
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|SearchType
name|searchType
parameter_list|)
block|{
name|this
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|,
name|numberOfShards
argument_list|,
name|searchType
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardSearchRequest
specifier|public
name|ShardSearchRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|SearchType
name|searchType
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|numberOfShards
operator|=
name|numberOfShards
expr_stmt|;
name|this
operator|.
name|searchType
operator|=
name|searchType
expr_stmt|;
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
DECL|method|shardId
specifier|public
name|int
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchType
return|;
block|}
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|numberOfShards
return|;
block|}
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|extraSource
specifier|public
name|BytesReference
name|extraSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|extraSource
return|;
block|}
DECL|method|source
specifier|public
name|ShardSearchRequest
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|extraSource
specifier|public
name|ShardSearchRequest
name|extraSource
parameter_list|(
name|BytesReference
name|extraSource
parameter_list|)
block|{
name|this
operator|.
name|extraSource
operator|=
name|extraSource
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|templateSource
specifier|public
name|BytesReference
name|templateSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|templateSource
return|;
block|}
DECL|method|templateName
specifier|public
name|String
name|templateName
parameter_list|()
block|{
return|return
name|templateName
return|;
block|}
DECL|method|templateParams
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
parameter_list|()
block|{
return|return
name|templateParams
return|;
block|}
DECL|method|nowInMillis
specifier|public
name|ShardSearchRequest
name|nowInMillis
parameter_list|(
name|long
name|nowInMillis
parameter_list|)
block|{
name|this
operator|.
name|nowInMillis
operator|=
name|nowInMillis
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|nowInMillis
specifier|public
name|long
name|nowInMillis
parameter_list|()
block|{
return|return
name|this
operator|.
name|nowInMillis
return|;
block|}
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|scroll
return|;
block|}
DECL|method|scroll
specifier|public
name|ShardSearchRequest
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|this
operator|.
name|scroll
operator|=
name|scroll
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filteringAliases
specifier|public
name|String
index|[]
name|filteringAliases
parameter_list|()
block|{
return|return
name|filteringAliases
return|;
block|}
DECL|method|filteringAliases
specifier|public
name|ShardSearchRequest
name|filteringAliases
parameter_list|(
name|String
index|[]
name|filteringAliases
parameter_list|)
block|{
name|this
operator|.
name|filteringAliases
operator|=
name|filteringAliases
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|types
return|;
block|}
DECL|method|types
specifier|public
name|ShardSearchRequest
name|types
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|shardId
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|searchType
operator|=
name|SearchType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|numberOfShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|scroll
operator|=
name|readScroll
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|extraSource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|filteringAliases
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|nowInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
name|templateSource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|templateName
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|templateParams
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
block|}
block|}
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|searchType
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|numberOfShards
argument_list|)
expr_stmt|;
if|if
condition|(
name|scroll
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scroll
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|extraSource
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|filteringAliases
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|nowInMillis
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|templateName
argument_list|)
expr_stmt|;
name|boolean
name|existTemplateParams
init|=
name|templateParams
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|existTemplateParams
argument_list|)
expr_stmt|;
if|if
condition|(
name|existTemplateParams
condition|)
block|{
name|out
operator|.
name|writeGenericValue
argument_list|(
name|templateParams
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

