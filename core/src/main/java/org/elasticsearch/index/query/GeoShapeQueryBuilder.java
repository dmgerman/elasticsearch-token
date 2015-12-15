begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|search
operator|.
name|BooleanClause
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
name|search
operator|.
name|BooleanQuery
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
name|search
operator|.
name|ConstantScoreQuery
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
name|search
operator|.
name|Query
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
name|spatial
operator|.
name|prefix
operator|.
name|PrefixTreeStrategy
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
name|spatial
operator|.
name|prefix
operator|.
name|RecursivePrefixTreeStrategy
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
name|spatial
operator|.
name|query
operator|.
name|SpatialArgs
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
name|spatial
operator|.
name|query
operator|.
name|SpatialOperation
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|BytesArray
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
name|geo
operator|.
name|ShapeRelation
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
name|geo
operator|.
name|ShapesAvailability
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
name|geo
operator|.
name|SpatialStrategy
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
name|geo
operator|.
name|builders
operator|.
name|ShapeBuilder
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
name|geo
operator|.
name|GeoShapeFieldMapper
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
name|internal
operator|.
name|SearchContext
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * {@link QueryBuilder} that builds a GeoShape Query  */
end_comment

begin_class
DECL|class|GeoShapeQueryBuilder
specifier|public
class|class
name|GeoShapeQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|GeoShapeQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_shape"
decl_stmt|;
DECL|field|DEFAULT_SHAPE_INDEX_NAME
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_SHAPE_INDEX_NAME
init|=
literal|"shapes"
decl_stmt|;
DECL|field|DEFAULT_SHAPE_FIELD_NAME
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_SHAPE_FIELD_NAME
init|=
literal|"shape"
decl_stmt|;
DECL|field|DEFAULT_SHAPE_RELATION
specifier|public
specifier|static
specifier|final
name|ShapeRelation
name|DEFAULT_SHAPE_RELATION
init|=
name|ShapeRelation
operator|.
name|INTERSECTS
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|GeoShapeQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|GeoShapeQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|new
name|BytesArray
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
comment|// TODO make the ShapeBuilder and subclasses Writable and implement hashCode
comment|// and Equals so ShapeBuilder can be used here
DECL|field|shapeBytes
specifier|private
name|BytesReference
name|shapeBytes
decl_stmt|;
DECL|field|strategy
specifier|private
name|SpatialStrategy
name|strategy
decl_stmt|;
DECL|field|indexedShapeId
specifier|private
specifier|final
name|String
name|indexedShapeId
decl_stmt|;
DECL|field|indexedShapeType
specifier|private
specifier|final
name|String
name|indexedShapeType
decl_stmt|;
DECL|field|indexedShapeIndex
specifier|private
name|String
name|indexedShapeIndex
init|=
name|DEFAULT_SHAPE_INDEX_NAME
decl_stmt|;
DECL|field|indexedShapePath
specifier|private
name|String
name|indexedShapePath
init|=
name|DEFAULT_SHAPE_FIELD_NAME
decl_stmt|;
DECL|field|relation
specifier|private
name|ShapeRelation
name|relation
init|=
name|DEFAULT_SHAPE_RELATION
decl_stmt|;
comment|/**      * Creates a new GeoShapeQueryBuilder whose Query will be against the given      * field name using the given Shape      *      * @param fieldName      *            Name of the field that will be queried      * @param shape      *            Shape used in the Query      */
DECL|method|GeoShapeQueryBuilder
specifier|public
name|GeoShapeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|ShapeBuilder
name|shape
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|shape
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new GeoShapeQueryBuilder whose Query will be against the given      * field name and will use the Shape found with the given ID in the given      * type      *      * @param fieldName      *            Name of the field that will be filtered      * @param indexedShapeId      *            ID of the indexed Shape that will be used in the Query      * @param indexedShapeType      *            Index type of the indexed Shapes      */
DECL|method|GeoShapeQueryBuilder
specifier|public
name|GeoShapeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
name|indexedShapeId
parameter_list|,
name|String
name|indexedShapeType
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
operator|(
name|BytesReference
operator|)
literal|null
argument_list|,
name|indexedShapeId
argument_list|,
name|indexedShapeType
argument_list|)
expr_stmt|;
block|}
DECL|method|GeoShapeQueryBuilder
name|GeoShapeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesReference
name|shapeBytes
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|shapeBytes
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|GeoShapeQueryBuilder
specifier|private
name|GeoShapeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|ShapeBuilder
name|shape
parameter_list|,
name|String
name|indexedShapeId
parameter_list|,
name|String
name|indexedShapeType
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
operator|new
name|BytesArray
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|indexedShapeId
argument_list|,
name|indexedShapeType
argument_list|)
expr_stmt|;
if|if
condition|(
name|shape
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|shapeBytes
operator|=
name|shape
operator|.
name|buildAsBytes
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|shapeBytes
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"shape must not be empty"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"shape must not be null"
argument_list|)
throw|;
block|}
block|}
DECL|method|GeoShapeQueryBuilder
specifier|private
name|GeoShapeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesReference
name|shapeBytes
parameter_list|,
name|String
name|indexedShapeId
parameter_list|,
name|String
name|indexedShapeType
parameter_list|)
block|{
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"fieldName is required"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|shapeBytes
operator|==
literal|null
operator|||
name|shapeBytes
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|)
operator|&&
name|indexedShapeId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"either shapeBytes or indexedShapeId and indexedShapeType are required"
argument_list|)
throw|;
block|}
if|if
condition|(
name|indexedShapeId
operator|!=
literal|null
operator|&&
name|indexedShapeType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"indexedShapeType is required if indexedShapeId is specified"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|shapeBytes
operator|=
name|shapeBytes
expr_stmt|;
name|this
operator|.
name|indexedShapeId
operator|=
name|indexedShapeId
expr_stmt|;
name|this
operator|.
name|indexedShapeType
operator|=
name|indexedShapeType
expr_stmt|;
block|}
comment|/**      * @return the name of the field that will be queried      */
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
comment|/**      * @return the JSON bytes for the shape used in the Query      */
DECL|method|shapeBytes
specifier|public
name|BytesReference
name|shapeBytes
parameter_list|()
block|{
return|return
name|shapeBytes
return|;
block|}
comment|/**      * @return the ID of the indexed Shape that will be used in the Query      */
DECL|method|indexedShapeId
specifier|public
name|String
name|indexedShapeId
parameter_list|()
block|{
return|return
name|indexedShapeId
return|;
block|}
comment|/**      * @return the document type of the indexed Shape that will be used in the      *         Query      */
DECL|method|indexedShapeType
specifier|public
name|String
name|indexedShapeType
parameter_list|()
block|{
return|return
name|indexedShapeType
return|;
block|}
comment|/**      * Defines which spatial strategy will be used for building the geo shape      * Query. When not set, the strategy that will be used will be the one that      * is associated with the geo shape field in the mappings.      *      * @param strategy      *            The spatial strategy to use for building the geo shape Query      * @return this      */
DECL|method|strategy
specifier|public
name|GeoShapeQueryBuilder
name|strategy
parameter_list|(
name|SpatialStrategy
name|strategy
parameter_list|)
block|{
if|if
condition|(
name|strategy
operator|!=
literal|null
operator|&&
name|strategy
operator|==
name|SpatialStrategy
operator|.
name|TERM
operator|&&
name|relation
operator|!=
name|ShapeRelation
operator|.
name|INTERSECTS
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"strategy ["
operator|+
name|strategy
operator|.
name|getStrategyName
argument_list|()
operator|+
literal|"] only supports relation ["
operator|+
name|ShapeRelation
operator|.
name|INTERSECTS
operator|.
name|getRelationName
argument_list|()
operator|+
literal|"] found relation ["
operator|+
name|relation
operator|.
name|getRelationName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|strategy
operator|=
name|strategy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return The spatial strategy to use for building the geo shape Query      */
DECL|method|strategy
specifier|public
name|SpatialStrategy
name|strategy
parameter_list|()
block|{
return|return
name|strategy
return|;
block|}
comment|/**      * Sets the name of the index where the indexed Shape can be found      *      * @param indexedShapeIndex Name of the index where the indexed Shape is      * @return this      */
DECL|method|indexedShapeIndex
specifier|public
name|GeoShapeQueryBuilder
name|indexedShapeIndex
parameter_list|(
name|String
name|indexedShapeIndex
parameter_list|)
block|{
name|this
operator|.
name|indexedShapeIndex
operator|=
name|indexedShapeIndex
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the index name for the indexed Shape that will be used in the      *         Query      */
DECL|method|indexedShapeIndex
specifier|public
name|String
name|indexedShapeIndex
parameter_list|()
block|{
return|return
name|indexedShapeIndex
return|;
block|}
comment|/**      * Sets the path of the field in the indexed Shape document that has the Shape itself      *      * @param indexedShapePath Path of the field where the Shape itself is defined      * @return this      */
DECL|method|indexedShapePath
specifier|public
name|GeoShapeQueryBuilder
name|indexedShapePath
parameter_list|(
name|String
name|indexedShapePath
parameter_list|)
block|{
name|this
operator|.
name|indexedShapePath
operator|=
name|indexedShapePath
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the path of the indexed Shape that will be used in the Query      */
DECL|method|indexedShapePath
specifier|public
name|String
name|indexedShapePath
parameter_list|()
block|{
return|return
name|indexedShapePath
return|;
block|}
comment|/**      * Sets the relation of query shape and indexed shape.      *      * @param relation relation of the shapes      * @return this      */
DECL|method|relation
specifier|public
name|GeoShapeQueryBuilder
name|relation
parameter_list|(
name|ShapeRelation
name|relation
parameter_list|)
block|{
if|if
condition|(
name|relation
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No Shape Relation defined"
argument_list|)
throw|;
block|}
if|if
condition|(
name|strategy
operator|!=
literal|null
operator|&&
name|strategy
operator|==
name|SpatialStrategy
operator|.
name|TERM
operator|&&
name|relation
operator|!=
name|ShapeRelation
operator|.
name|INTERSECTS
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"current strategy ["
operator|+
name|strategy
operator|.
name|getStrategyName
argument_list|()
operator|+
literal|"] only supports relation ["
operator|+
name|ShapeRelation
operator|.
name|INTERSECTS
operator|.
name|getRelationName
argument_list|()
operator|+
literal|"] found relation ["
operator|+
name|relation
operator|.
name|getRelationName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|relation
operator|=
name|relation
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @return the relation of query shape and indexed shape to use in the Query      */
DECL|method|relation
specifier|public
name|ShapeRelation
name|relation
parameter_list|()
block|{
return|return
name|relation
return|;
block|}
annotation|@
name|Override
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|ShapeBuilder
name|shape
decl_stmt|;
if|if
condition|(
name|shapeBytes
operator|==
literal|null
condition|)
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
name|indexedShapeIndex
argument_list|,
name|indexedShapeType
argument_list|,
name|indexedShapeId
argument_list|)
decl_stmt|;
name|getRequest
operator|.
name|copyContextAndHeadersFrom
argument_list|(
name|SearchContext
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
name|shape
operator|=
name|fetch
argument_list|(
name|context
operator|.
name|getClient
argument_list|()
argument_list|,
name|getRequest
argument_list|,
name|indexedShapePath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|XContentParser
name|shapeParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|shapeBytes
argument_list|)
decl_stmt|;
name|shapeParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|shape
operator|=
name|ShapeBuilder
operator|.
name|parse
argument_list|(
name|shapeParser
argument_list|)
expr_stmt|;
block|}
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"Failed to find geo_shape field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// TODO: This isn't the nicest way to check this
if|if
condition|(
operator|!
operator|(
name|fieldType
operator|instanceof
name|GeoShapeFieldMapper
operator|.
name|GeoShapeFieldType
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"Field ["
operator|+
name|fieldName
operator|+
literal|"] is not a geo_shape"
argument_list|)
throw|;
block|}
name|GeoShapeFieldMapper
operator|.
name|GeoShapeFieldType
name|shapeFieldType
init|=
operator|(
name|GeoShapeFieldMapper
operator|.
name|GeoShapeFieldType
operator|)
name|fieldType
decl_stmt|;
name|PrefixTreeStrategy
name|strategy
init|=
name|shapeFieldType
operator|.
name|defaultStrategy
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|strategy
operator|!=
literal|null
condition|)
block|{
name|strategy
operator|=
name|shapeFieldType
operator|.
name|resolveStrategy
argument_list|(
name|this
operator|.
name|strategy
argument_list|)
expr_stmt|;
block|}
name|Query
name|query
decl_stmt|;
if|if
condition|(
name|strategy
operator|instanceof
name|RecursivePrefixTreeStrategy
operator|&&
name|relation
operator|==
name|ShapeRelation
operator|.
name|DISJOINT
condition|)
block|{
comment|// this strategy doesn't support disjoint anymore: but it did
comment|// before, including creating lucene fieldcache (!)
comment|// in this case, execute disjoint as exists&& !intersects
name|BooleanQuery
operator|.
name|Builder
name|bool
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|Query
name|exists
init|=
name|ExistsQueryBuilder
operator|.
name|newFilter
argument_list|(
name|context
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
name|Query
name|intersects
init|=
name|strategy
operator|.
name|makeQuery
argument_list|(
name|getArgs
argument_list|(
name|shape
argument_list|,
name|ShapeRelation
operator|.
name|INTERSECTS
argument_list|)
argument_list|)
decl_stmt|;
name|bool
operator|.
name|add
argument_list|(
name|exists
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
name|bool
operator|.
name|add
argument_list|(
name|intersects
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|bool
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|strategy
operator|.
name|makeQuery
argument_list|(
name|getArgs
argument_list|(
name|shape
argument_list|,
name|relation
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
comment|/**      * Fetches the Shape with the given ID in the given type and index.      *      * @param getRequest      *            GetRequest containing index, type and id      * @param path      *            Name or path of the field in the Shape Document where the      *            Shape itself is located      * @return Shape with the given ID      * @throws IOException      *             Can be thrown while parsing the Shape Document and extracting      *             the Shape      */
DECL|method|fetch
specifier|private
name|ShapeBuilder
name|fetch
parameter_list|(
name|Client
name|client
parameter_list|,
name|GetRequest
name|getRequest
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|ShapesAvailability
operator|.
name|JTS_AVAILABLE
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"JTS not available"
argument_list|)
throw|;
block|}
name|getRequest
operator|.
name|preference
argument_list|(
literal|"_local"
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|operationThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|GetResponse
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|getRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|response
operator|.
name|isExists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Shape with ID ["
operator|+
name|getRequest
operator|.
name|id
argument_list|()
operator|+
literal|"] in type ["
operator|+
name|getRequest
operator|.
name|type
argument_list|()
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|String
index|[]
name|pathElements
init|=
name|Strings
operator|.
name|splitStringToArray
argument_list|(
name|path
argument_list|,
literal|'.'
argument_list|)
decl_stmt|;
name|int
name|currentPathSlot
init|=
literal|0
decl_stmt|;
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|response
operator|.
name|getSourceAsBytesRef
argument_list|()
argument_list|)
expr_stmt|;
name|XContentParser
operator|.
name|Token
name|currentToken
decl_stmt|;
while|while
condition|(
operator|(
name|currentToken
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
name|currentToken
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
if|if
condition|(
name|pathElements
index|[
name|currentPathSlot
index|]
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
operator|++
name|currentPathSlot
operator|==
name|pathElements
operator|.
name|length
condition|)
block|{
return|return
name|ShapeBuilder
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Shape with name ["
operator|+
name|getRequest
operator|.
name|id
argument_list|()
operator|+
literal|"] found but missing "
operator|+
name|path
operator|+
literal|" field"
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|getArgs
specifier|public
specifier|static
name|SpatialArgs
name|getArgs
parameter_list|(
name|ShapeBuilder
name|shape
parameter_list|,
name|ShapeRelation
name|relation
parameter_list|)
block|{
switch|switch
condition|(
name|relation
condition|)
block|{
case|case
name|DISJOINT
case|:
return|return
operator|new
name|SpatialArgs
argument_list|(
name|SpatialOperation
operator|.
name|IsDisjointTo
argument_list|,
name|shape
operator|.
name|build
argument_list|()
argument_list|)
return|;
case|case
name|INTERSECTS
case|:
return|return
operator|new
name|SpatialArgs
argument_list|(
name|SpatialOperation
operator|.
name|Intersects
argument_list|,
name|shape
operator|.
name|build
argument_list|()
argument_list|)
return|;
case|case
name|WITHIN
case|:
return|return
operator|new
name|SpatialArgs
argument_list|(
name|SpatialOperation
operator|.
name|IsWithin
argument_list|,
name|shape
operator|.
name|build
argument_list|()
argument_list|)
return|;
case|case
name|CONTAINS
case|:
return|return
operator|new
name|SpatialArgs
argument_list|(
name|SpatialOperation
operator|.
name|Contains
argument_list|,
name|shape
operator|.
name|build
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid relation ["
operator|+
name|relation
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
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
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|strategy
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|STRATEGY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|strategy
operator|.
name|getStrategyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shapeBytes
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|SHAPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|shapeBytes
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|GeoShapeQueryParser
operator|.
name|INDEXED_SHAPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|SHAPE_ID_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|indexedShapeId
argument_list|)
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|SHAPE_TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|indexedShapeType
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexedShapeIndex
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|SHAPE_INDEX_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|indexedShapeIndex
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexedShapePath
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|SHAPE_PATH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|indexedShapePath
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|relation
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GeoShapeQueryParser
operator|.
name|RELATION_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|relation
operator|.
name|getRelationName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|GeoShapeQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fieldName
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|GeoShapeQueryBuilder
name|builder
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|BytesReference
name|shapeBytes
init|=
name|in
operator|.
name|readBytesReference
argument_list|()
decl_stmt|;
name|builder
operator|=
operator|new
name|GeoShapeQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|shapeBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|indexedShapeId
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|String
name|indexedShapeType
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|String
name|indexedShapeIndex
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|String
name|indexedShapePath
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|builder
operator|=
operator|new
name|GeoShapeQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|indexedShapeId
argument_list|,
name|indexedShapeType
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexedShapeIndex
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|indexedShapeIndex
operator|=
name|indexedShapeIndex
expr_stmt|;
block|}
if|if
condition|(
name|indexedShapePath
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|indexedShapePath
operator|=
name|indexedShapePath
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|relation
operator|=
name|ShapeRelation
operator|.
name|DISJOINT
operator|.
name|readFrom
argument_list|(
name|in
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
name|builder
operator|.
name|strategy
operator|=
name|SpatialStrategy
operator|.
name|RECURSIVE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
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
name|fieldName
argument_list|)
expr_stmt|;
name|boolean
name|hasShapeBytes
init|=
name|shapeBytes
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasShapeBytes
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasShapeBytes
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|shapeBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexedShapeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexedShapeType
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexedShapeIndex
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexedShapePath
argument_list|)
expr_stmt|;
block|}
name|relation
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|strategy
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
name|strategy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|GeoShapeQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldName
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|indexedShapeId
argument_list|,
name|other
operator|.
name|indexedShapeId
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|indexedShapeIndex
argument_list|,
name|other
operator|.
name|indexedShapeIndex
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|indexedShapePath
argument_list|,
name|other
operator|.
name|indexedShapePath
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|indexedShapeType
argument_list|,
name|other
operator|.
name|indexedShapeType
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|relation
argument_list|,
name|other
operator|.
name|relation
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|shapeBytes
argument_list|,
name|other
operator|.
name|shapeBytes
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|strategy
argument_list|,
name|other
operator|.
name|strategy
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|fieldName
argument_list|,
name|indexedShapeId
argument_list|,
name|indexedShapeIndex
argument_list|,
name|indexedShapePath
argument_list|,
name|indexedShapeType
argument_list|,
name|relation
argument_list|,
name|shapeBytes
argument_list|,
name|strategy
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

