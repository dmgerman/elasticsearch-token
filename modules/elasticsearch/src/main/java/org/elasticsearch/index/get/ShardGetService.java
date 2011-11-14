begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|document
operator|.
name|Document
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
name|document
operator|.
name|Fieldable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|BytesHolder
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
name|document
operator|.
name|ResetFieldSelector
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
name|UidField
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
name|index
operator|.
name|cache
operator|.
name|IndexCache
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
name|FieldMappers
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
name|mapper
operator|.
name|internal
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
name|internal
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
name|internal
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
name|internal
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
name|mapper
operator|.
name|selector
operator|.
name|FieldMappersFieldSelector
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
name|settings
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
name|service
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
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SourceLookup
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
name|Map
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShardGetService
specifier|public
class|class
name|ShardGetService
extends|extends
name|AbstractIndexShardComponent
block|{
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|indexCache
specifier|private
specifier|final
name|IndexCache
name|indexCache
decl_stmt|;
DECL|field|indexShard
specifier|private
name|IndexShard
name|indexShard
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
DECL|method|ShardGetService
annotation|@
name|Inject
specifier|public
name|ShardGetService
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexCache
name|indexCache
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|indexCache
operator|=
name|indexCache
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
argument_list|)
return|;
block|}
comment|// sadly, to overcome cyclic dep, we need to do this and inject it ourselves...
DECL|method|setIndexShard
specifier|public
name|ShardGetService
name|setIndexShard
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
return|return
name|this
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
parameter_list|)
throws|throws
name|ElasticSearchException
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
argument_list|)
decl_stmt|;
if|if
condition|(
name|getResult
operator|.
name|exists
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
DECL|method|innerGet
specifier|public
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
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|boolean
name|loadSource
init|=
name|gFields
operator|==
literal|null
operator|||
name|gFields
operator|.
name|length
operator|>
literal|0
decl_stmt|;
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
name|UidFieldMapper
operator|.
name|TERM_FACTORY
operator|.
name|createTerm
argument_list|(
name|Uid
operator|.
name|createUid
argument_list|(
name|typeX
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
operator|.
name|loadSource
argument_list|(
name|loadSource
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
name|index
argument_list|()
operator|.
name|name
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
name|index
argument_list|()
operator|.
name|name
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
name|UidFieldMapper
operator|.
name|TERM_FACTORY
operator|.
name|createTerm
argument_list|(
name|Uid
operator|.
name|createUid
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
operator|.
name|loadSource
argument_list|(
name|loadSource
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
name|index
argument_list|()
operator|.
name|name
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
operator|==
literal|null
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
name|index
argument_list|()
operator|.
name|name
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
try|try
block|{
comment|// break between having loaded it from translog (so we only have _source), and having a document to load
if|if
condition|(
name|get
operator|.
name|docIdAndVersion
argument_list|()
operator|!=
literal|null
condition|)
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
name|byte
index|[]
name|source
init|=
literal|null
decl_stmt|;
name|UidField
operator|.
name|DocIdAndVersion
name|docIdAndVersion
init|=
name|get
operator|.
name|docIdAndVersion
argument_list|()
decl_stmt|;
name|ResetFieldSelector
name|fieldSelector
init|=
name|buildFieldSelectors
argument_list|(
name|docMapper
argument_list|,
name|gFields
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldSelector
operator|!=
literal|null
condition|)
block|{
name|fieldSelector
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Document
name|doc
decl_stmt|;
try|try
block|{
name|doc
operator|=
name|docIdAndVersion
operator|.
name|reader
operator|.
name|document
argument_list|(
name|docIdAndVersion
operator|.
name|docId
argument_list|,
name|fieldSelector
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
name|ElasticSearchException
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
name|extractSource
argument_list|(
name|doc
argument_list|,
name|docMapper
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|oField
range|:
name|doc
operator|.
name|getFields
argument_list|()
control|)
block|{
name|Fieldable
name|field
init|=
operator|(
name|Fieldable
operator|)
name|oField
decl_stmt|;
name|String
name|name
init|=
name|field
operator|.
name|name
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
literal|null
decl_stmt|;
name|FieldMappers
name|fieldMappers
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|indexName
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
name|FieldMapper
name|mapper
init|=
name|fieldMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
expr_stmt|;
name|value
operator|=
name|mapper
operator|.
name|valueForSearch
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|field
operator|.
name|isBinary
argument_list|()
condition|)
block|{
name|value
operator|=
name|field
operator|.
name|getBinaryValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|field
operator|.
name|stringValue
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newHashMapWithExpectedSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|GetField
name|getField
init|=
name|fields
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|getField
operator|==
literal|null
condition|)
block|{
name|getField
operator|=
operator|new
name|GetField
argument_list|(
name|name
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|getField
argument_list|)
expr_stmt|;
block|}
name|getField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|field
operator|.
name|contains
argument_list|(
literal|"_source."
argument_list|)
operator|||
name|field
operator|.
name|contains
argument_list|(
literal|"doc["
argument_list|)
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
name|indexCache
operator|.
name|fieldData
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SearchScript
name|searchScript
init|=
name|scriptService
operator|.
name|search
argument_list|(
name|searchLookup
argument_list|,
literal|"mvel"
argument_list|,
name|field
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|searchScript
operator|.
name|setNextReader
argument_list|(
name|docIdAndVersion
operator|.
name|reader
argument_list|)
expr_stmt|;
name|searchScript
operator|.
name|setNextDocId
argument_list|(
name|docIdAndVersion
operator|.
name|docId
argument_list|)
expr_stmt|;
try|try
block|{
name|value
operator|=
name|searchScript
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to execute get request script field [{}]"
argument_list|,
name|e
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
comment|// ignore
block|}
block|}
else|else
block|{
name|FieldMappers
name|x
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|==
literal|null
operator|||
operator|!
name|x
operator|.
name|mapper
argument_list|()
operator|.
name|stored
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
name|indexCache
operator|.
name|fieldData
argument_list|()
argument_list|)
expr_stmt|;
name|searchLookup
operator|.
name|setNextReader
argument_list|(
name|docIdAndVersion
operator|.
name|reader
argument_list|)
expr_stmt|;
name|searchLookup
operator|.
name|setNextDocId
argument_list|(
name|docIdAndVersion
operator|.
name|docId
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|searchLookup
operator|.
name|source
argument_list|()
operator|.
name|extractValue
argument_list|(
name|field
argument_list|)
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
name|newHashMapWithExpectedSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|GetField
name|getField
init|=
name|fields
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|getField
operator|==
literal|null
condition|)
block|{
name|getField
operator|=
operator|new
name|GetField
argument_list|(
name|field
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|getField
argument_list|)
expr_stmt|;
block|}
name|getField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
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
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BytesHolder
argument_list|(
name|source
argument_list|)
argument_list|,
name|fields
argument_list|)
return|;
block|}
else|else
block|{
name|Translog
operator|.
name|Source
name|source
init|=
name|get
operator|.
name|source
argument_list|()
decl_stmt|;
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
name|boolean
name|sourceRequested
init|=
literal|false
decl_stmt|;
comment|// we can only load scripts that can run against the source
if|if
condition|(
name|gFields
operator|==
literal|null
condition|)
block|{
name|sourceRequested
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|gFields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// no fields, and no source
name|sourceRequested
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
init|=
literal|null
decl_stmt|;
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
if|if
condition|(
name|field
operator|.
name|equals
argument_list|(
literal|"_source"
argument_list|)
condition|)
block|{
name|sourceRequested
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
name|Object
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|field
operator|.
name|equals
argument_list|(
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|)
operator|&&
name|docMapper
operator|.
name|routingFieldMapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|value
operator|=
name|source
operator|.
name|routing
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|field
operator|.
name|equals
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
operator|&&
name|docMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|!=
literal|null
operator|&&
name|docMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|value
operator|=
name|source
operator|.
name|parent
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|field
operator|.
name|equals
argument_list|(
name|TimestampFieldMapper
operator|.
name|NAME
argument_list|)
operator|&&
name|docMapper
operator|.
name|timestampFieldMapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|value
operator|=
name|source
operator|.
name|timestamp
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|field
operator|.
name|equals
argument_list|(
name|TTLFieldMapper
operator|.
name|NAME
argument_list|)
operator|&&
name|docMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
comment|// Call value for search with timestamp + ttl here to display the live remaining ttl value and be consistent with the search result display
if|if
condition|(
name|source
operator|.
name|ttl
operator|>
literal|0
condition|)
block|{
name|value
operator|=
name|docMapper
operator|.
name|TTLFieldMapper
argument_list|()
operator|.
name|valueForSearch
argument_list|(
name|source
operator|.
name|timestamp
operator|+
name|source
operator|.
name|ttl
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|field
operator|.
name|contains
argument_list|(
literal|"_source."
argument_list|)
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
name|indexCache
operator|.
name|fieldData
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sourceAsMap
operator|==
literal|null
condition|)
block|{
name|sourceAsMap
operator|=
name|SourceLookup
operator|.
name|sourceAsMap
argument_list|(
name|source
operator|.
name|source
operator|.
name|bytes
argument_list|()
argument_list|,
name|source
operator|.
name|source
operator|.
name|offset
argument_list|()
argument_list|,
name|source
operator|.
name|source
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SearchScript
name|searchScript
init|=
name|scriptService
operator|.
name|search
argument_list|(
name|searchLookup
argument_list|,
literal|"mvel"
argument_list|,
name|field
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// we can't do this, only allow to run scripts against the source
comment|//searchScript.setNextReader(docIdAndVersion.reader);
comment|//searchScript.setNextDocId(docIdAndVersion.docId);
comment|// but, we need to inject the parsed source into the script, so it will be used...
name|searchScript
operator|.
name|setNextSource
argument_list|(
name|sourceAsMap
argument_list|)
expr_stmt|;
try|try
block|{
name|value
operator|=
name|searchScript
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to execute get request script field [{}]"
argument_list|,
name|e
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
comment|// ignore
block|}
block|}
else|else
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
name|indexCache
operator|.
name|fieldData
argument_list|()
argument_list|)
expr_stmt|;
name|searchLookup
operator|.
name|source
argument_list|()
operator|.
name|setNextSource
argument_list|(
name|SourceLookup
operator|.
name|sourceAsMap
argument_list|(
name|source
operator|.
name|source
operator|.
name|bytes
argument_list|()
argument_list|,
name|source
operator|.
name|source
operator|.
name|offset
argument_list|()
argument_list|,
name|source
operator|.
name|source
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|searchLookup
operator|.
name|source
argument_list|()
operator|.
name|extractValue
argument_list|(
name|field
argument_list|)
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
name|newHashMapWithExpectedSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|GetField
name|getField
init|=
name|fields
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|getField
operator|==
literal|null
condition|)
block|{
name|getField
operator|=
operator|new
name|GetField
argument_list|(
name|field
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|getField
argument_list|)
expr_stmt|;
block|}
name|getField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|GetResult
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
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
name|sourceRequested
condition|?
name|source
operator|.
name|source
else|:
literal|null
argument_list|,
name|fields
argument_list|)
return|;
block|}
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
DECL|method|buildFieldSelectors
specifier|private
specifier|static
name|ResetFieldSelector
name|buildFieldSelectors
parameter_list|(
name|DocumentMapper
name|docMapper
parameter_list|,
name|String
modifier|...
name|fields
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
return|return
name|docMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|fieldSelector
argument_list|()
return|;
block|}
comment|// don't load anything
if|if
condition|(
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|FieldMappersFieldSelector
name|fieldSelector
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|fields
control|)
block|{
name|FieldMappers
name|x
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|!=
literal|null
operator|&&
name|x
operator|.
name|mapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
if|if
condition|(
name|fieldSelector
operator|==
literal|null
condition|)
block|{
name|fieldSelector
operator|=
operator|new
name|FieldMappersFieldSelector
argument_list|()
expr_stmt|;
block|}
name|fieldSelector
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fieldSelector
return|;
block|}
DECL|method|extractSource
specifier|private
specifier|static
name|byte
index|[]
name|extractSource
parameter_list|(
name|Document
name|doc
parameter_list|,
name|DocumentMapper
name|documentMapper
parameter_list|)
block|{
name|byte
index|[]
name|source
init|=
literal|null
decl_stmt|;
name|Fieldable
name|sourceField
init|=
name|doc
operator|.
name|getFieldable
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sourceField
operator|!=
literal|null
condition|)
block|{
name|source
operator|=
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|nativeValue
argument_list|(
name|sourceField
argument_list|)
expr_stmt|;
name|doc
operator|.
name|removeField
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
block|}
end_class

end_unit

