begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
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
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|common
operator|.
name|metrics
operator|.
name|CounterMetric
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
name|metrics
operator|.
name|MeanMetric
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
name|util
operator|.
name|set
operator|.
name|Sets
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
name|XContentHelper
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
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
name|VersionType
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
name|engine
operator|.
name|Engine
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
name|fieldvisitor
operator|.
name|CustomFieldsVisitor
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
name|fieldvisitor
operator|.
name|FieldsVisitor
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
name|DocumentMapper
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
name|mapper
operator|.
name|RoutingFieldMapper
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
name|SourceFieldMapper
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
name|TTLFieldMapper
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
name|TimestampFieldMapper
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
name|Uid
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
name|UidFieldMapper
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
name|AbstractIndexShardComponent
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
name|IndexShard
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
name|translog
operator|.
name|Translog
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
name|subphase
operator|.
name|FetchSourceContext
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
name|subphase
operator|.
name|ParentFieldSubFetchPhase
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
name|lookup
operator|.
name|LeafSearchLookup
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
name|lookup
operator|.
name|SearchLookup
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
name|Arrays
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
DECL|class|ShardGetService
specifier|public
specifier|final
class|class
name|ShardGetService
extends|extends
name|AbstractIndexShardComponent
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|existsMetric
specifier|private
specifier|final
name|MeanMetric
name|existsMetric
init|=
operator|new
name|MeanMetric
argument_list|()
decl_stmt|;
DECL|field|missingMetric
specifier|private
specifier|final
name|MeanMetric
name|missingMetric
init|=
operator|new
name|MeanMetric
argument_list|()
decl_stmt|;
DECL|field|currentMetric
specifier|private
specifier|final
name|CounterMetric
name|currentMetric
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|method|ShardGetService
specifier|public
name|ShardGetService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|super
argument_list|(
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
block|}
DECL|method|stats
specifier|public
name|GetStats
name|stats
parameter_list|()
block|{
return|return
operator|new
name|GetStats
argument_list|(
name|existsMetric
operator|.
name|count
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|existsMetric
operator|.
name|sum
argument_list|()
argument_list|)
argument_list|,
name|missingMetric
operator|.
name|count
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|missingMetric
operator|.
name|sum
argument_list|()
argument_list|)
argument_list|,
name|currentMetric
operator|.
name|count
argument_list|()
argument_list|)
return|;
block|}
DECL|method|get
specifier|public
name|GetResult
name|get
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
index|[]
name|gFields
parameter_list|,
name|boolean
name|realtime
parameter_list|,
name|long
name|version
parameter_list|,
name|VersionType
name|versionType
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|,
name|boolean
name|ignoreErrorsOnGeneratedFields
parameter_list|)
block|{
name|currentMetric
operator|.
name|inc
argument_list|()
expr_stmt|;
try|try
block|{
name|long
name|now
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|GetResult
name|getResult
init|=
name|innerGet
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|gFields
argument_list|,
name|realtime
argument_list|,
name|version
argument_list|,
name|versionType
argument_list|,
name|fetchSourceContext
argument_list|,
name|ignoreErrorsOnGeneratedFields
argument_list|)
decl_stmt|;
if|if
condition|(
name|getResult
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|existsMetric
operator|.
name|inc
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|now
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|missingMetric
operator|.
name|inc
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|now
argument_list|)
expr_stmt|;
block|}
return|return
name|getResult
return|;
block|}
finally|finally
block|{
name|currentMetric
operator|.
name|dec
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Returns {@link GetResult} based on the specified {@link org.elasticsearch.index.engine.Engine.GetResult} argument.      * This method basically loads specified fields for the associated document in the engineGetResult.      * This method load the fields from the Lucene index and not from transaction log and therefore isn't realtime.      *<p>      * Note: Call<b>must</b> release engine searcher associated with engineGetResult!      */
DECL|method|get
specifier|public
name|GetResult
name|get
parameter_list|(
name|Engine
operator|.
name|GetResult
name|engineGetResult
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
index|[]
name|fields
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|)
block|{
if|if
condition|(
operator|!
name|engineGetResult
operator|.
name|exists
argument_list|()
condition|)
block|{
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
name|currentMetric
operator|.
name|inc
argument_list|()
expr_stmt|;
try|try
block|{
name|long
name|now
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|fetchSourceContext
operator|=
name|normalizeFetchSourceContent
argument_list|(
name|fetchSourceContext
argument_list|,
name|fields
argument_list|)
expr_stmt|;
name|GetResult
name|getResult
init|=
name|innerGetLoadFromStoredFields
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|fields
argument_list|,
name|fetchSourceContext
argument_list|,
name|engineGetResult
argument_list|,
name|mapperService
argument_list|)
decl_stmt|;
if|if
condition|(
name|getResult
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|existsMetric
operator|.
name|inc
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|now
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|missingMetric
operator|.
name|inc
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|now
argument_list|)
expr_stmt|;
comment|// This shouldn't happen...
block|}
return|return
name|getResult
return|;
block|}
finally|finally
block|{
name|currentMetric
operator|.
name|dec
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * decides what needs to be done based on the request input and always returns a valid non-null FetchSourceContext      */
DECL|method|normalizeFetchSourceContent
specifier|private
name|FetchSourceContext
name|normalizeFetchSourceContent
parameter_list|(
annotation|@
name|Nullable
name|FetchSourceContext
name|context
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|gFields
parameter_list|)
block|{
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
return|return
name|context
return|;
block|}
if|if
condition|(
name|gFields
operator|==
literal|null
condition|)
block|{
return|return
name|FetchSourceContext
operator|.
name|FETCH_SOURCE
return|;
block|}
for|for
control|(
name|String
name|field
range|:
name|gFields
control|)
block|{
if|if
condition|(
name|SourceFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|field
argument_list|)
condition|)
block|{
return|return
name|FetchSourceContext
operator|.
name|FETCH_SOURCE
return|;
block|}
block|}
return|return
name|FetchSourceContext
operator|.
name|DO_NOT_FETCH_SOURCE
return|;
block|}
DECL|method|innerGet
specifier|private
name|GetResult
name|innerGet
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
index|[]
name|gFields
parameter_list|,
name|boolean
name|realtime
parameter_list|,
name|long
name|version
parameter_list|,
name|VersionType
name|versionType
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|,
name|boolean
name|ignoreErrorsOnGeneratedFields
parameter_list|)
block|{
name|fetchSourceContext
operator|=
name|normalizeFetchSourceContent
argument_list|(
name|fetchSourceContext
argument_list|,
name|gFields
argument_list|)
expr_stmt|;
name|Engine
operator|.
name|GetResult
name|get
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
operator|||
name|type
operator|.
name|equals
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|typeX
range|:
name|mapperService
operator|.
name|types
argument_list|()
control|)
block|{
name|get
operator|=
name|indexShard
operator|.
name|get
argument_list|(
operator|new
name|Engine
operator|.
name|Get
argument_list|(
name|realtime
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|typeX
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
operator|.
name|version
argument_list|(
name|version
argument_list|)
operator|.
name|versionType
argument_list|(
name|versionType
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|get
operator|.
name|exists
argument_list|()
condition|)
block|{
name|type
operator|=
name|typeX
expr_stmt|;
break|break;
block|}
else|else
block|{
name|get
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|get
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
if|if
condition|(
operator|!
name|get
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// no need to release here as well..., we release in the for loop for non exists
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|get
operator|=
name|indexShard
operator|.
name|get
argument_list|(
operator|new
name|Engine
operator|.
name|Get
argument_list|(
name|realtime
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
operator|.
name|version
argument_list|(
name|version
argument_list|)
operator|.
name|versionType
argument_list|(
name|versionType
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|get
operator|.
name|exists
argument_list|()
condition|)
block|{
name|get
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
try|try
block|{
comment|// break between having loaded it from translog (so we only have _source), and having a document to load
return|return
name|innerGetLoadFromStoredFields
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|gFields
argument_list|,
name|fetchSourceContext
argument_list|,
name|get
argument_list|,
name|mapperService
argument_list|)
return|;
block|}
finally|finally
block|{
name|get
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|innerGetLoadFromStoredFields
specifier|private
name|GetResult
name|innerGetLoadFromStoredFields
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
index|[]
name|gFields
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|,
name|Engine
operator|.
name|GetResult
name|get
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
init|=
literal|null
decl_stmt|;
name|BytesReference
name|source
init|=
literal|null
decl_stmt|;
name|Versions
operator|.
name|DocIdAndVersion
name|docIdAndVersion
init|=
name|get
operator|.
name|docIdAndVersion
argument_list|()
decl_stmt|;
name|FieldsVisitor
name|fieldVisitor
init|=
name|buildFieldsVisitors
argument_list|(
name|gFields
argument_list|,
name|fetchSourceContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldVisitor
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|docIdAndVersion
operator|.
name|context
operator|.
name|reader
argument_list|()
operator|.
name|document
argument_list|(
name|docIdAndVersion
operator|.
name|docId
argument_list|,
name|fieldVisitor
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Failed to get type ["
operator|+
name|type
operator|+
literal|"] and id ["
operator|+
name|id
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|source
operator|=
name|fieldVisitor
operator|.
name|source
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|fieldVisitor
operator|.
name|fields
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fieldVisitor
operator|.
name|postProcess
argument_list|(
name|mapperService
argument_list|)
expr_stmt|;
name|fields
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|fieldVisitor
operator|.
name|fields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|fieldVisitor
operator|.
name|fields
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|fields
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|GetField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|DocumentMapper
name|docMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|docMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|active
argument_list|()
condition|)
block|{
name|String
name|parentId
init|=
name|ParentFieldSubFetchPhase
operator|.
name|getParentId
argument_list|(
name|docMapper
operator|.
name|parentFieldMapper
argument_list|()
argument_list|,
name|docIdAndVersion
operator|.
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|docIdAndVersion
operator|.
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|fields
operator|.
name|put
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
operator|new
name|GetField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|parentId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// now, go and do the script thingy if needed
if|if
condition|(
name|gFields
operator|!=
literal|null
operator|&&
name|gFields
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|SearchLookup
name|searchLookup
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|gFields
control|)
block|{
name|Object
name|value
init|=
literal|null
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|docMapper
operator|.
name|objectMappers
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// Only fail if we know it is a object field, missing paths / fields shouldn't fail.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field ["
operator|+
name|field
operator|+
literal|"] isn't a leaf field"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
operator|&&
operator|!
name|fieldMapper
operator|.
name|isGenerated
argument_list|()
condition|)
block|{
if|if
condition|(
name|searchLookup
operator|==
literal|null
condition|)
block|{
name|searchLookup
operator|=
operator|new
name|SearchLookup
argument_list|(
name|mapperService
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
name|type
block|}
argument_list|)
expr_stmt|;
name|LeafSearchLookup
name|leafSearchLookup
init|=
name|searchLookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|docIdAndVersion
operator|.
name|context
argument_list|)
decl_stmt|;
name|searchLookup
operator|.
name|source
argument_list|()
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|leafSearchLookup
operator|.
name|setDocument
argument_list|(
name|docIdAndVersion
operator|.
name|docId
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|searchLookup
operator|.
name|source
argument_list|()
operator|.
name|extractRawValues
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|value
operator|=
name|values
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
operator|new
name|GetField
argument_list|(
name|field
argument_list|,
operator|(
name|List
operator|)
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
operator|new
name|GetField
argument_list|(
name|field
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
condition|)
block|{
name|source
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fetchSourceContext
operator|.
name|includes
argument_list|()
operator|.
name|length
operator|>
literal|0
operator|||
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
decl_stmt|;
name|XContentType
name|sourceContentType
init|=
literal|null
decl_stmt|;
comment|// TODO: The source might parsed and available in the sourceLookup but that one uses unordered maps so different. Do we care?
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|typeMapTuple
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|source
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|sourceContentType
operator|=
name|typeMapTuple
operator|.
name|v1
argument_list|()
expr_stmt|;
name|sourceAsMap
operator|=
name|typeMapTuple
operator|.
name|v2
argument_list|()
expr_stmt|;
name|sourceAsMap
operator|=
name|XContentMapValues
operator|.
name|filter
argument_list|(
name|sourceAsMap
argument_list|,
name|fetchSourceContext
operator|.
name|includes
argument_list|()
argument_list|,
name|fetchSourceContext
operator|.
name|excludes
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|source
operator|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|sourceContentType
argument_list|)
operator|.
name|map
argument_list|(
name|sourceAsMap
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Failed to get type ["
operator|+
name|type
operator|+
literal|"] and id ["
operator|+
name|id
operator|+
literal|"] with includes/excludes set"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|get
operator|.
name|version
argument_list|()
argument_list|,
name|get
operator|.
name|exists
argument_list|()
argument_list|,
name|source
argument_list|,
name|fields
argument_list|)
return|;
block|}
DECL|method|buildFieldsVisitors
specifier|private
specifier|static
name|FieldsVisitor
name|buildFieldsVisitors
parameter_list|(
name|String
index|[]
name|fields
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
operator|||
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
condition|?
operator|new
name|FieldsVisitor
argument_list|(
literal|true
argument_list|)
else|:
literal|null
return|;
block|}
return|return
operator|new
name|CustomFieldsVisitor
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|fields
argument_list|)
argument_list|,
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

