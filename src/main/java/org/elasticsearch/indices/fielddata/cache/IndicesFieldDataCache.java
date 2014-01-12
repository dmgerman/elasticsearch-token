begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.fielddata.cache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|fielddata
operator|.
name|cache
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
name|cache
operator|.
name|*
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
name|index
operator|.
name|AtomicReaderContext
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
name|index
operator|.
name|SegmentReader
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
name|lucene
operator|.
name|SegmentReaderUtils
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
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|Index
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
name|fielddata
operator|.
name|AtomicFieldData
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
name|fielddata
operator|.
name|FieldDataType
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
name|fielddata
operator|.
name|IndexFieldData
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
name|fielddata
operator|.
name|IndexFieldDataCache
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
name|FieldMapper
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
name|service
operator|.
name|IndexService
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
name|shard
operator|.
name|ShardId
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
name|shard
operator|.
name|ShardUtils
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
name|shard
operator|.
name|service
operator|.
name|IndexShard
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|IndicesFieldDataCache
specifier|public
class|class
name|IndicesFieldDataCache
extends|extends
name|AbstractComponent
implements|implements
name|RemovalListener
argument_list|<
name|IndicesFieldDataCache
operator|.
name|Key
argument_list|,
name|AtomicFieldData
argument_list|>
block|{
DECL|field|cache
name|Cache
argument_list|<
name|Key
argument_list|,
name|AtomicFieldData
argument_list|>
name|cache
decl_stmt|;
DECL|field|size
specifier|private
specifier|volatile
name|String
name|size
decl_stmt|;
DECL|field|sizeInBytes
specifier|private
specifier|volatile
name|long
name|sizeInBytes
decl_stmt|;
DECL|field|expire
specifier|private
specifier|volatile
name|TimeValue
name|expire
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndicesFieldDataCache
specifier|public
name|IndicesFieldDataCache
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"size"
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|this
operator|.
name|sizeInBytes
operator|=
name|componentSettings
operator|.
name|getAsMemory
argument_list|(
literal|"size"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|expire
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"expire"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|buildCache
argument_list|()
expr_stmt|;
block|}
DECL|method|buildCache
specifier|private
name|void
name|buildCache
parameter_list|()
block|{
name|CacheBuilder
argument_list|<
name|Key
argument_list|,
name|AtomicFieldData
argument_list|>
name|cacheBuilder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|removalListener
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|sizeInBytes
operator|>
literal|0
condition|)
block|{
name|cacheBuilder
operator|.
name|maximumWeight
argument_list|(
name|sizeInBytes
argument_list|)
operator|.
name|weigher
argument_list|(
operator|new
name|FieldDataWeigher
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// defaults to 4, but this is a busy map for all indices, increase it a bit
name|cacheBuilder
operator|.
name|concurrencyLevel
argument_list|(
literal|16
argument_list|)
expr_stmt|;
if|if
condition|(
name|expire
operator|!=
literal|null
operator|&&
name|expire
operator|.
name|millis
argument_list|()
operator|>
literal|0
condition|)
block|{
name|cacheBuilder
operator|.
name|expireAfterAccess
argument_list|(
name|expire
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"using size [{}] [{}], expire [{}]"
argument_list|,
name|size
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeInBytes
argument_list|)
argument_list|,
name|expire
argument_list|)
expr_stmt|;
name|cache
operator|=
name|cacheBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|cache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
DECL|method|buildIndexFieldDataCache
specifier|public
name|IndexFieldDataCache
name|buildIndexFieldDataCache
parameter_list|(
annotation|@
name|Nullable
name|IndexService
name|indexService
parameter_list|,
name|Index
name|index
parameter_list|,
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|)
block|{
return|return
operator|new
name|IndexFieldCache
argument_list|(
name|indexService
argument_list|,
name|index
argument_list|,
name|fieldNames
argument_list|,
name|fieldDataType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|onRemoval
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|Key
argument_list|,
name|AtomicFieldData
argument_list|>
name|notification
parameter_list|)
block|{
name|Key
name|key
init|=
name|notification
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
operator|||
name|key
operator|.
name|listener
operator|==
literal|null
condition|)
block|{
return|return;
comment|// nothing to do here really...
block|}
name|IndexFieldCache
name|indexCache
init|=
name|key
operator|.
name|indexCache
decl_stmt|;
name|long
name|sizeInBytes
init|=
name|key
operator|.
name|sizeInBytes
decl_stmt|;
name|AtomicFieldData
name|value
init|=
name|notification
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|sizeInBytes
operator|==
operator|-
literal|1
operator|&&
name|value
operator|!=
literal|null
condition|)
block|{
name|sizeInBytes
operator|=
name|value
operator|.
name|getMemorySizeInBytes
argument_list|()
expr_stmt|;
block|}
name|key
operator|.
name|listener
operator|.
name|onUnload
argument_list|(
name|indexCache
operator|.
name|fieldNames
argument_list|,
name|indexCache
operator|.
name|fieldDataType
argument_list|,
name|notification
operator|.
name|wasEvicted
argument_list|()
argument_list|,
name|sizeInBytes
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|class|FieldDataWeigher
specifier|public
specifier|static
class|class
name|FieldDataWeigher
implements|implements
name|Weigher
argument_list|<
name|Key
argument_list|,
name|AtomicFieldData
argument_list|>
block|{
annotation|@
name|Override
DECL|method|weigh
specifier|public
name|int
name|weigh
parameter_list|(
name|Key
name|key
parameter_list|,
name|AtomicFieldData
name|fieldData
parameter_list|)
block|{
name|int
name|weight
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|fieldData
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
return|return
name|weight
operator|==
literal|0
condition|?
literal|1
else|:
name|weight
return|;
block|}
block|}
comment|/**      * A specific cache instance for the relevant parameters of it (index, fieldNames, fieldType).      */
DECL|class|IndexFieldCache
class|class
name|IndexFieldCache
implements|implements
name|IndexFieldDataCache
implements|,
name|SegmentReader
operator|.
name|CoreClosedListener
block|{
annotation|@
name|Nullable
DECL|field|indexService
specifier|private
specifier|final
name|IndexService
name|indexService
decl_stmt|;
DECL|field|index
specifier|final
name|Index
name|index
decl_stmt|;
DECL|field|fieldNames
specifier|final
name|FieldMapper
operator|.
name|Names
name|fieldNames
decl_stmt|;
DECL|field|fieldDataType
specifier|final
name|FieldDataType
name|fieldDataType
decl_stmt|;
DECL|method|IndexFieldCache
name|IndexFieldCache
parameter_list|(
annotation|@
name|Nullable
name|IndexService
name|indexService
parameter_list|,
name|Index
name|index
parameter_list|,
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|)
block|{
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|fieldNames
operator|=
name|fieldNames
expr_stmt|;
name|this
operator|.
name|fieldDataType
operator|=
name|fieldDataType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
parameter_list|<
name|FD
extends|extends
name|AtomicFieldData
parameter_list|,
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|FD
argument_list|>
parameter_list|>
name|FD
name|load
parameter_list|(
specifier|final
name|AtomicReaderContext
name|context
parameter_list|,
specifier|final
name|IFD
name|indexFieldData
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Key
name|key
init|=
operator|new
name|Key
argument_list|(
name|this
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
decl_stmt|;
comment|//noinspection unchecked
return|return
operator|(
name|FD
operator|)
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|,
operator|new
name|Callable
argument_list|<
name|AtomicFieldData
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|AtomicFieldData
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|SegmentReaderUtils
operator|.
name|registerCoreListener
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|IndexFieldCache
operator|.
name|this
argument_list|)
expr_stmt|;
name|AtomicFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|loadDirect
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|!=
literal|null
condition|)
block|{
name|ShardId
name|shardId
init|=
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|key
operator|.
name|listener
operator|=
name|shard
operator|.
name|fieldData
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|key
operator|.
name|listener
operator|!=
literal|null
condition|)
block|{
name|key
operator|.
name|listener
operator|.
name|onLoad
argument_list|(
name|fieldNames
argument_list|,
name|fieldDataType
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
block|}
return|return
name|fieldData
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|onClose
specifier|public
name|void
name|onClose
parameter_list|(
name|Object
name|coreKey
parameter_list|)
block|{
name|cache
operator|.
name|invalidate
argument_list|(
operator|new
name|Key
argument_list|(
name|this
argument_list|,
name|coreKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
for|for
control|(
name|Key
name|key
range|:
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|.
name|indexCache
operator|.
name|index
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|cache
operator|.
name|invalidate
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
for|for
control|(
name|Key
name|key
range|:
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|.
name|indexCache
operator|.
name|index
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
if|if
condition|(
name|key
operator|.
name|indexCache
operator|.
name|fieldNames
operator|.
name|fullName
argument_list|()
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|cache
operator|.
name|invalidate
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|Object
name|coreCacheKey
parameter_list|)
block|{
name|cache
operator|.
name|invalidate
argument_list|(
operator|new
name|Key
argument_list|(
name|this
argument_list|,
name|coreCacheKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Key
specifier|public
specifier|static
class|class
name|Key
block|{
DECL|field|indexCache
specifier|public
specifier|final
name|IndexFieldCache
name|indexCache
decl_stmt|;
DECL|field|readerKey
specifier|public
specifier|final
name|Object
name|readerKey
decl_stmt|;
annotation|@
name|Nullable
DECL|field|listener
specifier|public
name|IndexFieldDataCache
operator|.
name|Listener
name|listener
decl_stmt|;
comment|// optional stats listener
DECL|field|sizeInBytes
name|long
name|sizeInBytes
init|=
operator|-
literal|1
decl_stmt|;
comment|// optional size in bytes (we keep it here in case the values are soft references)
DECL|method|Key
name|Key
parameter_list|(
name|IndexFieldCache
name|indexCache
parameter_list|,
name|Object
name|readerKey
parameter_list|)
block|{
name|this
operator|.
name|indexCache
operator|=
name|indexCache
expr_stmt|;
name|this
operator|.
name|readerKey
operator|=
name|readerKey
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
name|Key
name|key
init|=
operator|(
name|Key
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|indexCache
operator|.
name|equals
argument_list|(
name|key
operator|.
name|indexCache
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|readerKey
operator|.
name|equals
argument_list|(
name|key
operator|.
name|readerKey
argument_list|)
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
name|indexCache
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|readerKey
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

