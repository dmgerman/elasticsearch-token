begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|util
operator|.
name|Accountable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|collect
operator|.
name|MapBuilder
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
name|collect
operator|.
name|Tuple
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|AbstractIndexComponent
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
name|IndexSettings
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
name|plain
operator|.
name|AbstractGeoPointDVIndexFieldData
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
name|plain
operator|.
name|BytesBinaryDVIndexFieldData
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
name|plain
operator|.
name|DocValuesIndexFieldData
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
name|plain
operator|.
name|GeoPointArrayIndexFieldData
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
name|plain
operator|.
name|IndexIndexFieldData
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
name|plain
operator|.
name|PagedBytesIndexFieldData
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
name|plain
operator|.
name|ParentChildIndexFieldData
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
name|MappedFieldType
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
name|MapperService
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
name|BooleanFieldMapper
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
name|KeywordFieldMapper
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
name|TextFieldMapper
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
name|internal
operator|.
name|IndexFieldMapper
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
name|internal
operator|.
name|ParentFieldMapper
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
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCache
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Collection
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|IndexFieldDataService
specifier|public
class|class
name|IndexFieldDataService
extends|extends
name|AbstractIndexComponent
implements|implements
name|Closeable
block|{
DECL|field|FIELDDATA_CACHE_VALUE_NODE
specifier|public
specifier|static
specifier|final
name|String
name|FIELDDATA_CACHE_VALUE_NODE
init|=
literal|"node"
decl_stmt|;
DECL|field|FIELDDATA_CACHE_KEY
specifier|public
specifier|static
specifier|final
name|String
name|FIELDDATA_CACHE_KEY
init|=
literal|"index.fielddata.cache"
decl_stmt|;
DECL|field|INDEX_FIELDDATA_CACHE_KEY
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|INDEX_FIELDDATA_CACHE_KEY
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
name|FIELDDATA_CACHE_KEY
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|FIELDDATA_CACHE_VALUE_NODE
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
block|{
lambda|switch (s
argument_list|)
block|{
case|case
literal|"node"
case|:
case|case
literal|"none"
case|:
return|return
name|s
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"failed to parse ["
operator|+
name|s
operator|+
literal|"] must be one of [node,node]"
argument_list|)
throw|;
block|}
end_class

begin_expr_stmt
unit|},
name|Property
operator|.
name|IndexScope
end_expr_stmt

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_decl_stmt
DECL|field|circuitBreakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
end_decl_stmt

begin_decl_stmt
DECL|field|indicesFieldDataCache
specifier|private
specifier|final
name|IndicesFieldDataCache
name|indicesFieldDataCache
decl_stmt|;
end_decl_stmt

begin_comment
comment|// the below map needs to be modified under a lock
end_comment

begin_decl_stmt
DECL|field|fieldDataCaches
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|IndexFieldDataCache
argument_list|>
name|fieldDataCaches
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
end_decl_stmt

begin_decl_stmt
DECL|field|DEFAULT_NOOP_LISTENER
specifier|private
specifier|static
specifier|final
name|IndexFieldDataCache
operator|.
name|Listener
name|DEFAULT_NOOP_LISTENER
init|=
operator|new
name|IndexFieldDataCache
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onCache
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|Accountable
name|ramUsage
parameter_list|)
block|{         }
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
parameter_list|)
block|{         }
block|}
decl_stmt|;
end_decl_stmt

begin_decl_stmt
DECL|field|listener
specifier|private
specifier|volatile
name|IndexFieldDataCache
operator|.
name|Listener
name|listener
init|=
name|DEFAULT_NOOP_LISTENER
decl_stmt|;
end_decl_stmt

begin_constructor
DECL|method|IndexFieldDataService
specifier|public
name|IndexFieldDataService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|IndicesFieldDataCache
name|indicesFieldDataCache
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesFieldDataCache
operator|=
name|indicesFieldDataCache
expr_stmt|;
name|this
operator|.
name|circuitBreakerService
operator|=
name|circuitBreakerService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
block|}
end_constructor

begin_function
DECL|method|clear
specifier|public
specifier|synchronized
name|void
name|clear
parameter_list|()
block|{
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|Collection
argument_list|<
name|IndexFieldDataCache
argument_list|>
name|fieldDataCacheValues
init|=
name|fieldDataCaches
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexFieldDataCache
name|cache
range|:
name|fieldDataCacheValues
control|)
block|{
try|try
block|{
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
name|fieldDataCacheValues
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ExceptionsHelper
operator|.
name|maybeThrowRuntimeAndSuppress
argument_list|(
name|exceptions
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|clearField
specifier|public
specifier|synchronized
name|void
name|clearField
parameter_list|(
specifier|final
name|String
name|fieldName
parameter_list|)
block|{
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|IndexFieldDataCache
name|cache
init|=
name|fieldDataCaches
operator|.
name|remove
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|cache
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
name|ExceptionsHelper
operator|.
name|maybeThrowRuntimeAndSuppress
argument_list|(
name|exceptions
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|getForField
specifier|public
parameter_list|<
name|IFD
extends|extends
name|IndexFieldData
argument_list|<
name|?
argument_list|>
parameter_list|>
name|IFD
name|getForField
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|fieldType
operator|.
name|name
argument_list|()
decl_stmt|;
name|IndexFieldData
operator|.
name|Builder
name|builder
init|=
name|fieldType
operator|.
name|fielddataBuilder
argument_list|()
decl_stmt|;
name|IndexFieldDataCache
name|cache
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|cache
operator|=
name|fieldDataCaches
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|cache
operator|==
literal|null
condition|)
block|{
name|String
name|cacheType
init|=
name|indexSettings
operator|.
name|getValue
argument_list|(
name|INDEX_FIELDDATA_CACHE_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|FIELDDATA_CACHE_VALUE_NODE
operator|.
name|equals
argument_list|(
name|cacheType
argument_list|)
condition|)
block|{
name|cache
operator|=
name|indicesFieldDataCache
operator|.
name|buildIndexFieldDataCache
argument_list|(
name|listener
argument_list|,
name|index
argument_list|()
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"none"
operator|.
name|equals
argument_list|(
name|cacheType
argument_list|)
condition|)
block|{
name|cache
operator|=
operator|new
name|IndexFieldDataCache
operator|.
name|None
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"cache type not supported ["
operator|+
name|cacheType
operator|+
literal|"] for field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|fieldDataCaches
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|IFD
operator|)
name|builder
operator|.
name|build
argument_list|(
name|indexSettings
argument_list|,
name|fieldType
argument_list|,
name|cache
argument_list|,
name|circuitBreakerService
argument_list|,
name|mapperService
argument_list|)
return|;
block|}
end_function

begin_comment
comment|/**      * Sets a {@link org.elasticsearch.index.fielddata.IndexFieldDataCache.Listener} passed to each {@link IndexFieldData}      * creation to capture onCache and onRemoval events. Setting a listener on this method will override any previously      * set listeners.      * @throws IllegalStateException if the listener is set more than once      */
end_comment

begin_function
DECL|method|setListener
specifier|public
name|void
name|setListener
parameter_list|(
name|IndexFieldDataCache
operator|.
name|Listener
name|listener
parameter_list|)
block|{
if|if
condition|(
name|listener
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"listener must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|listener
operator|!=
name|DEFAULT_NOOP_LISTENER
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"can't set listener more than once"
argument_list|)
throw|;
block|}
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|clear
argument_list|()
expr_stmt|;
block|}
end_function

unit|}
end_unit

