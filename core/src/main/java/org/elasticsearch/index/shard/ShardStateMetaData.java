begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|AllocationId
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|CorruptStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|MetaDataStateFormat
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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShardStateMetaData
specifier|public
specifier|final
class|class
name|ShardStateMetaData
block|{
DECL|field|SHARD_STATE_FILE_PREFIX
specifier|private
specifier|static
specifier|final
name|String
name|SHARD_STATE_FILE_PREFIX
init|=
literal|"state-"
decl_stmt|;
DECL|field|PRIMARY_KEY
specifier|private
specifier|static
specifier|final
name|String
name|PRIMARY_KEY
init|=
literal|"primary"
decl_stmt|;
DECL|field|VERSION_KEY
specifier|private
specifier|static
specifier|final
name|String
name|VERSION_KEY
init|=
literal|"version"
decl_stmt|;
DECL|field|INDEX_UUID_KEY
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_UUID_KEY
init|=
literal|"index_uuid"
decl_stmt|;
DECL|field|ALLOCATION_ID_KEY
specifier|private
specifier|static
specifier|final
name|String
name|ALLOCATION_ID_KEY
init|=
literal|"allocation_id"
decl_stmt|;
DECL|field|version
specifier|public
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|indexUUID
specifier|public
specifier|final
name|String
name|indexUUID
decl_stmt|;
DECL|field|primary
specifier|public
specifier|final
name|boolean
name|primary
decl_stmt|;
annotation|@
name|Nullable
DECL|field|allocationId
specifier|public
specifier|final
name|AllocationId
name|allocationId
decl_stmt|;
comment|// can be null if we read from legacy format (see fromXContent)
DECL|method|ShardStateMetaData
specifier|public
name|ShardStateMetaData
parameter_list|(
name|long
name|version
parameter_list|,
name|boolean
name|primary
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|AllocationId
name|allocationId
parameter_list|)
block|{
assert|assert
name|indexUUID
operator|!=
literal|null
assert|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
name|this
operator|.
name|allocationId
operator|=
name|allocationId
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
block|{
return|return
literal|true
return|;
block|}
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
block|{
return|return
literal|false
return|;
block|}
name|ShardStateMetaData
name|that
init|=
operator|(
name|ShardStateMetaData
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|primary
operator|!=
name|that
operator|.
name|primary
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|version
operator|!=
name|that
operator|.
name|version
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|indexUUID
operator|!=
literal|null
condition|?
operator|!
name|indexUUID
operator|.
name|equals
argument_list|(
name|that
operator|.
name|indexUUID
argument_list|)
else|:
name|that
operator|.
name|indexUUID
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|allocationId
operator|!=
literal|null
condition|?
operator|!
name|allocationId
operator|.
name|equals
argument_list|(
name|that
operator|.
name|allocationId
argument_list|)
else|:
name|that
operator|.
name|allocationId
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
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
name|Long
operator|.
name|hashCode
argument_list|(
name|version
argument_list|)
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|indexUUID
operator|!=
literal|null
condition|?
name|indexUUID
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
name|allocationId
operator|!=
literal|null
condition|?
name|allocationId
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
name|primary
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
literal|"version ["
operator|+
name|version
operator|+
literal|"], primary ["
operator|+
name|primary
operator|+
literal|"], allocation ["
operator|+
name|allocationId
operator|+
literal|"]"
return|;
block|}
DECL|field|FORMAT
specifier|public
specifier|static
specifier|final
name|MetaDataStateFormat
argument_list|<
name|ShardStateMetaData
argument_list|>
name|FORMAT
init|=
operator|new
name|MetaDataStateFormat
argument_list|<
name|ShardStateMetaData
argument_list|>
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|,
name|SHARD_STATE_FILE_PREFIX
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|XContentBuilder
name|newXContentBuilder
parameter_list|(
name|XContentType
name|type
parameter_list|,
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|xContentBuilder
init|=
name|super
operator|.
name|newXContentBuilder
argument_list|(
name|type
argument_list|,
name|stream
argument_list|)
decl_stmt|;
name|xContentBuilder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
return|return
name|xContentBuilder
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|ShardStateMetaData
name|shardStateMetaData
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|VERSION_KEY
argument_list|,
name|shardStateMetaData
operator|.
name|version
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|PRIMARY_KEY
argument_list|,
name|shardStateMetaData
operator|.
name|primary
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|INDEX_UUID_KEY
argument_list|,
name|shardStateMetaData
operator|.
name|indexUUID
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|ALLOCATION_ID_KEY
argument_list|,
name|shardStateMetaData
operator|.
name|allocationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ShardStateMetaData
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
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
return|return
literal|null
return|;
block|}
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
name|Boolean
name|primary
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|String
name|indexUUID
init|=
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
decl_stmt|;
name|AllocationId
name|allocationId
init|=
literal|null
decl_stmt|;
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|VERSION_KEY
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|version
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|PRIMARY_KEY
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|primary
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|INDEX_UUID_KEY
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|indexUUID
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"unexpected field in shard state ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
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
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|ALLOCATION_ID_KEY
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|allocationId
operator|=
name|AllocationId
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"unexpected object in shard state ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"unexpected token in shard state ["
operator|+
name|token
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|primary
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"missing value for [primary] in shard state"
argument_list|)
throw|;
block|}
if|if
condition|(
name|version
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"missing value for [version] in shard state"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ShardStateMetaData
argument_list|(
name|version
argument_list|,
name|primary
argument_list|,
name|indexUUID
argument_list|,
name|allocationId
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

