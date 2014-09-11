begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntArrayList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|LongArrayList
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
name|action
operator|.
name|support
operator|.
name|single
operator|.
name|shard
operator|.
name|SingleShardOperationRequest
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
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|VersionType
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
name|fetch
operator|.
name|source
operator|.
name|FetchSourceContext
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

begin_class
DECL|class|MultiGetShardRequest
specifier|public
class|class
name|MultiGetShardRequest
extends|extends
name|SingleShardOperationRequest
argument_list|<
name|MultiGetShardRequest
argument_list|>
block|{
DECL|field|shardId
specifier|private
name|int
name|shardId
decl_stmt|;
DECL|field|preference
specifier|private
name|String
name|preference
decl_stmt|;
DECL|field|realtime
name|Boolean
name|realtime
decl_stmt|;
DECL|field|refresh
name|boolean
name|refresh
decl_stmt|;
DECL|field|ignoreErrorsOnGeneratedFields
name|boolean
name|ignoreErrorsOnGeneratedFields
init|=
literal|false
decl_stmt|;
DECL|field|locations
name|IntArrayList
name|locations
decl_stmt|;
DECL|field|items
name|List
argument_list|<
name|MultiGetRequest
operator|.
name|Item
argument_list|>
name|items
decl_stmt|;
DECL|method|MultiGetShardRequest
name|MultiGetShardRequest
parameter_list|()
block|{      }
DECL|method|MultiGetShardRequest
name|MultiGetShardRequest
parameter_list|(
name|MultiGetRequest
name|multiGetRequest
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|super
argument_list|(
name|multiGetRequest
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|locations
operator|=
operator|new
name|IntArrayList
argument_list|()
expr_stmt|;
name|items
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|preference
operator|=
name|multiGetRequest
operator|.
name|preference
expr_stmt|;
name|realtime
operator|=
name|multiGetRequest
operator|.
name|realtime
expr_stmt|;
name|refresh
operator|=
name|multiGetRequest
operator|.
name|refresh
expr_stmt|;
name|ignoreErrorsOnGeneratedFields
operator|=
name|multiGetRequest
operator|.
name|ignoreErrorsOnGeneratedFields
expr_stmt|;
block|}
DECL|method|shardId
specifier|public
name|int
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|preference
specifier|public
name|MultiGetShardRequest
name|preference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|this
operator|.
name|preference
operator|=
name|preference
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|String
name|preference
parameter_list|()
block|{
return|return
name|this
operator|.
name|preference
return|;
block|}
DECL|method|realtime
specifier|public
name|boolean
name|realtime
parameter_list|()
block|{
return|return
name|this
operator|.
name|realtime
operator|==
literal|null
condition|?
literal|true
else|:
name|this
operator|.
name|realtime
return|;
block|}
DECL|method|realtime
specifier|public
name|MultiGetShardRequest
name|realtime
parameter_list|(
name|Boolean
name|realtime
parameter_list|)
block|{
name|this
operator|.
name|realtime
operator|=
name|realtime
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ignoreErrorsOnGeneratedFields
specifier|public
name|MultiGetShardRequest
name|ignoreErrorsOnGeneratedFields
parameter_list|(
name|Boolean
name|ignoreErrorsOnGeneratedFields
parameter_list|)
block|{
name|this
operator|.
name|ignoreErrorsOnGeneratedFields
operator|=
name|ignoreErrorsOnGeneratedFields
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|refresh
specifier|public
name|boolean
name|refresh
parameter_list|()
block|{
return|return
name|this
operator|.
name|refresh
return|;
block|}
DECL|method|refresh
specifier|public
name|MultiGetShardRequest
name|refresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|this
operator|.
name|refresh
operator|=
name|refresh
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
name|void
name|add
parameter_list|(
name|int
name|location
parameter_list|,
name|MultiGetRequest
operator|.
name|Item
name|item
parameter_list|)
block|{
name|this
operator|.
name|locations
operator|.
name|add
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|items
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
name|String
index|[]
name|indices
init|=
operator|new
name|String
index|[
name|items
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|index
argument_list|()
expr_stmt|;
block|}
return|return
name|indices
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|locations
operator|=
operator|new
name|IntArrayList
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|items
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
name|items
operator|.
name|add
argument_list|(
name|MultiGetRequest
operator|.
name|Item
operator|.
name|readItem
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|types
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ids
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
index|[]
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|LongArrayList
name|versions
init|=
operator|new
name|LongArrayList
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|VersionType
argument_list|>
name|versionTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FetchSourceContext
argument_list|>
name|fetchSourceContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|types
operator|.
name|add
argument_list|(
name|in
operator|.
name|readSharedString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|types
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|ids
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|size1
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size1
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|fieldsArray
init|=
operator|new
name|String
index|[
name|size1
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size1
condition|;
name|j
operator|++
control|)
block|{
name|fieldsArray
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
name|fieldsArray
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|versions
operator|.
name|add
argument_list|(
name|Versions
operator|.
name|readVersionWithVLongForBW
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|versionTypes
operator|.
name|add
argument_list|(
name|VersionType
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fetchSourceContexts
operator|.
name|add
argument_list|(
name|FetchSourceContext
operator|.
name|optionalReadFromStream
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
comment|//before 1.4 we have only one index, the concrete one
name|MultiGetRequest
operator|.
name|Item
name|item
init|=
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
name|index
argument_list|,
name|types
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ids
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|fields
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|version
argument_list|(
name|versions
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|versionType
argument_list|(
name|versionTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|fetchSourceContext
argument_list|(
name|fetchSourceContexts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|items
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
block|}
name|preference
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|refresh
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|byte
name|realtime
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|realtime
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|realtime
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|realtime
operator|==
literal|1
condition|)
block|{
name|this
operator|.
name|realtime
operator|=
literal|true
expr_stmt|;
block|}
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
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
name|ignoreErrorsOnGeneratedFields
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
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
name|writeVInt
argument_list|(
name|locations
operator|.
name|size
argument_list|()
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
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|locations
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|locations
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|MultiGetRequest
operator|.
name|Item
name|item
init|=
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|item
operator|.
name|type
argument_list|()
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
name|out
operator|.
name|writeSharedString
argument_list|(
name|item
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeString
argument_list|(
name|item
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|item
operator|.
name|fields
argument_list|()
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|item
operator|.
name|fields
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|item
operator|.
name|fields
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
name|Versions
operator|.
name|writeVersionWithVLongForBW
argument_list|(
name|item
operator|.
name|version
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|item
operator|.
name|versionType
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|FetchSourceContext
operator|.
name|optionalWriteToStream
argument_list|(
name|item
operator|.
name|fetchSourceContext
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|preference
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
if|if
condition|(
name|realtime
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|realtime
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
block|}
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
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|ignoreErrorsOnGeneratedFields
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|ignoreErrorsOnGeneratedFields
specifier|public
name|boolean
name|ignoreErrorsOnGeneratedFields
parameter_list|()
block|{
return|return
name|ignoreErrorsOnGeneratedFields
return|;
block|}
block|}
end_class

end_unit

