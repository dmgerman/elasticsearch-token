begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|IndexReader
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
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
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
literal|"40%"
argument_list|)
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
name|computeSizeInBytes
argument_list|()
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
name|cache
operator|=
name|cacheBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|computeSizeInBytes
specifier|private
name|void
name|computeSizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
name|double
name|percent
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|size
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|size
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|sizeInBytes
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|percent
operator|/
literal|100
operator|)
operator|*
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|getMem
argument_list|()
operator|.
name|getHeapMax
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sizeInBytes
operator|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|size
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
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
parameter_list|,
name|IndexFieldDataCache
operator|.
name|Listener
name|listener
parameter_list|)
block|{
return|return
operator|new
name|IndexFieldCache
argument_list|(
name|index
argument_list|,
name|fieldNames
argument_list|,
name|fieldDataType
argument_list|,
name|listener
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
if|if
condition|(
name|notification
operator|.
name|getKey
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|IndexFieldCache
name|indexFieldCache
init|=
name|notification
operator|.
name|getKey
argument_list|()
operator|.
name|indexCache
decl_stmt|;
name|indexFieldCache
operator|.
name|listener
operator|.
name|onUnload
argument_list|(
name|indexFieldCache
operator|.
name|index
argument_list|,
name|indexFieldCache
operator|.
name|fieldNames
argument_list|,
name|indexFieldCache
operator|.
name|fieldDataType
argument_list|,
name|notification
operator|.
name|wasEvicted
argument_list|()
argument_list|,
name|notification
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
DECL|field|listener
specifier|final
name|Listener
name|listener
decl_stmt|;
DECL|method|IndexFieldCache
name|IndexFieldCache
parameter_list|(
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
parameter_list|,
name|Listener
name|listener
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
name|this
operator|.
name|listener
operator|=
name|listener
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
if|if
condition|(
name|context
operator|.
name|reader
argument_list|()
operator|instanceof
name|SegmentReader
condition|)
block|{
operator|(
operator|(
name|SegmentReader
operator|)
name|context
operator|.
name|reader
argument_list|()
operator|)
operator|.
name|addCoreClosedListener
argument_list|(
name|IndexFieldCache
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
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
name|listener
operator|.
name|onLoad
argument_list|(
name|index
argument_list|,
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
argument_list|,
name|fieldDataType
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
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
name|SegmentReader
name|owner
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
name|owner
operator|.
name|getCoreCacheKey
argument_list|()
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
parameter_list|(
name|Index
name|index
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
name|Index
name|index
parameter_list|,
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
name|Index
name|index
parameter_list|,
name|IndexReader
name|reader
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
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
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

