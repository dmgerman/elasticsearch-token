begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.geo.builders
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|builders
package|;
end_package

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Shape
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
name|XShapeCollection
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
name|xcontent
operator|.
name|XContentBuilder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_class
DECL|class|GeometryCollectionBuilder
specifier|public
class|class
name|GeometryCollectionBuilder
extends|extends
name|ShapeBuilder
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|GeoShapeType
name|TYPE
init|=
name|GeoShapeType
operator|.
name|GEOMETRYCOLLECTION
decl_stmt|;
DECL|field|PROTOTYPE
specifier|public
specifier|static
specifier|final
name|GeometryCollectionBuilder
name|PROTOTYPE
init|=
operator|new
name|GeometryCollectionBuilder
argument_list|()
decl_stmt|;
DECL|field|shapes
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|ShapeBuilder
argument_list|>
name|shapes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|shape
specifier|public
name|GeometryCollectionBuilder
name|shape
parameter_list|(
name|ShapeBuilder
name|shape
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|shape
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|point
specifier|public
name|GeometryCollectionBuilder
name|point
parameter_list|(
name|PointBuilder
name|point
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|point
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|multiPoint
specifier|public
name|GeometryCollectionBuilder
name|multiPoint
parameter_list|(
name|MultiPointBuilder
name|multiPoint
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|multiPoint
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|line
specifier|public
name|GeometryCollectionBuilder
name|line
parameter_list|(
name|LineStringBuilder
name|line
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|line
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|multiLine
specifier|public
name|GeometryCollectionBuilder
name|multiLine
parameter_list|(
name|MultiLineStringBuilder
name|multiLine
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|multiLine
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|polygon
specifier|public
name|GeometryCollectionBuilder
name|polygon
parameter_list|(
name|PolygonBuilder
name|polygon
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|polygon
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|multiPolygon
specifier|public
name|GeometryCollectionBuilder
name|multiPolygon
parameter_list|(
name|MultiPolygonBuilder
name|multiPolygon
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|multiPolygon
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|envelope
specifier|public
name|GeometryCollectionBuilder
name|envelope
parameter_list|(
name|EnvelopeBuilder
name|envelope
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|envelope
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|circle
specifier|public
name|GeometryCollectionBuilder
name|circle
parameter_list|(
name|CircleBuilder
name|circle
parameter_list|)
block|{
name|this
operator|.
name|shapes
operator|.
name|add
argument_list|(
name|circle
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|getShapeAt
specifier|public
name|ShapeBuilder
name|getShapeAt
parameter_list|(
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|>=
name|this
operator|.
name|shapes
operator|.
name|size
argument_list|()
operator|||
name|i
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"GeometryCollection contains "
operator|+
name|this
operator|.
name|shapes
operator|.
name|size
argument_list|()
operator|+
literal|" shapes. + "
operator|+
literal|"No shape found at index "
operator|+
name|i
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|shapes
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
DECL|method|numShapes
specifier|public
name|int
name|numShapes
parameter_list|()
block|{
return|return
name|this
operator|.
name|shapes
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FIELD_TYPE
argument_list|,
name|TYPE
operator|.
name|shapeName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|FIELD_GEOMETRIES
argument_list|)
expr_stmt|;
for|for
control|(
name|ShapeBuilder
name|shape
range|:
name|shapes
control|)
block|{
name|shape
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|GeoShapeType
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|Shape
name|build
parameter_list|()
block|{
name|List
argument_list|<
name|Shape
argument_list|>
name|shapes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|this
operator|.
name|shapes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShapeBuilder
name|shape
range|:
name|this
operator|.
name|shapes
control|)
block|{
name|shapes
operator|.
name|add
argument_list|(
name|shape
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shapes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
return|return
name|shapes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
else|else
return|return
operator|new
name|XShapeCollection
argument_list|<>
argument_list|(
name|shapes
argument_list|,
name|SPATIAL_CONTEXT
argument_list|)
return|;
comment|//note: ShapeCollection is probably faster than a Multi* geom.
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|shapes
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
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|GeometryCollectionBuilder
name|other
init|=
operator|(
name|GeometryCollectionBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|shapes
argument_list|,
name|other
operator|.
name|shapes
argument_list|)
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
name|writeVInt
argument_list|(
name|shapes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ShapeBuilder
name|shape
range|:
name|shapes
control|)
block|{
name|out
operator|.
name|writeShape
argument_list|(
name|shape
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|GeometryCollectionBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|GeometryCollectionBuilder
name|geometryCollectionBuilder
init|=
operator|new
name|GeometryCollectionBuilder
argument_list|()
decl_stmt|;
name|int
name|shapes
init|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|shapes
condition|;
name|i
operator|++
control|)
block|{
name|geometryCollectionBuilder
operator|.
name|shape
argument_list|(
name|in
operator|.
name|readShape
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|geometryCollectionBuilder
return|;
block|}
block|}
end_class

end_unit

