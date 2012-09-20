begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
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
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|impl
operator|.
name|RectangleImpl
import|;
end_import

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
name|jts
operator|.
name|JtsGeometry
import|;
end_import

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
name|jts
operator|.
name|JtsPoint
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|Coordinate
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|GeometryFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|LinearRing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|Locale
import|;
end_import

begin_comment
comment|/**  * Parsers which supports reading {@link Shape}s in GeoJSON format from a given  * {@link XContentParser}.  *<p/>  * An example of the format used for polygons:  *<p/>  * {  * "type": "Polygon",  * "coordinates": [  * [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],  * [100.0, 1.0], [100.0, 0.0] ]  * ]  * }  *<p/>  * Note, currently MultiPolygon and GeometryCollections are not supported  */
end_comment

begin_class
DECL|class|GeoJSONShapeParser
specifier|public
class|class
name|GeoJSONShapeParser
block|{
DECL|field|GEOMETRY_FACTORY
specifier|private
specifier|static
specifier|final
name|GeometryFactory
name|GEOMETRY_FACTORY
init|=
operator|new
name|GeometryFactory
argument_list|()
decl_stmt|;
DECL|method|GeoJSONShapeParser
specifier|private
name|GeoJSONShapeParser
parameter_list|()
block|{     }
comment|/**      * Parses the current object from the given {@link XContentParser}, creating      * the {@link Shape} representation      *      * @param parser Parser that will be read from      * @return Shape representation of the geojson defined Shape      * @throws IOException Thrown if an error occurs while reading from the XContentParser      */
DECL|method|parse
specifier|public
specifier|static
name|Shape
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Shape must be an object consisting of type and coordinates"
argument_list|)
throw|;
block|}
name|String
name|shapeType
init|=
literal|null
decl_stmt|;
name|CoordinateNode
name|node
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
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
name|String
name|fieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"type"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|shapeType
operator|=
name|parser
operator|.
name|text
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|)
expr_stmt|;
if|if
condition|(
name|shapeType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Unknown Shape type ["
operator|+
name|parser
operator|.
name|text
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"coordinates"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|node
operator|=
name|parseCoordinates
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|shapeType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Shape type not included"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Coordinates not included"
argument_list|)
throw|;
block|}
return|return
name|buildShape
argument_list|(
name|shapeType
argument_list|,
name|node
argument_list|)
return|;
block|}
comment|/**      * Recursive method which parses the arrays of coordinates used to define Shapes      *      * @param parser Parser that will be read from      * @return CoordinateNode representing the start of the coordinate tree      * @throws IOException Thrown if an error occurs while reading from the XContentParser      */
DECL|method|parseCoordinates
specifier|private
specifier|static
name|CoordinateNode
name|parseCoordinates
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
comment|// Base case
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
name|double
name|lon
init|=
name|parser
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|double
name|lat
init|=
name|parser
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
operator|new
name|CoordinateNode
argument_list|(
operator|new
name|Coordinate
argument_list|(
name|lon
argument_list|,
name|lat
argument_list|)
argument_list|)
return|;
block|}
name|List
argument_list|<
name|CoordinateNode
argument_list|>
name|nodes
init|=
operator|new
name|ArrayList
argument_list|<
name|CoordinateNode
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|nodes
operator|.
name|add
argument_list|(
name|parseCoordinates
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|CoordinateNode
argument_list|(
name|nodes
argument_list|)
return|;
block|}
comment|/**      * Builds the actual {@link Shape} with the given shape type from the tree      * of coordinates      *      * @param shapeType Type of Shape to be built      * @param node      Root node of the coordinate tree      * @return Shape built from the coordinates      */
DECL|method|buildShape
specifier|private
specifier|static
name|Shape
name|buildShape
parameter_list|(
name|String
name|shapeType
parameter_list|,
name|CoordinateNode
name|node
parameter_list|)
block|{
if|if
condition|(
literal|"point"
operator|.
name|equals
argument_list|(
name|shapeType
argument_list|)
condition|)
block|{
return|return
operator|new
name|JtsPoint
argument_list|(
name|GEOMETRY_FACTORY
operator|.
name|createPoint
argument_list|(
name|node
operator|.
name|coordinate
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"linestring"
operator|.
name|equals
argument_list|(
name|shapeType
argument_list|)
condition|)
block|{
return|return
operator|new
name|JtsGeometry
argument_list|(
name|GEOMETRY_FACTORY
operator|.
name|createLineString
argument_list|(
name|toCoordinates
argument_list|(
name|node
argument_list|)
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|true
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"polygon"
operator|.
name|equals
argument_list|(
name|shapeType
argument_list|)
condition|)
block|{
name|LinearRing
name|shell
init|=
name|GEOMETRY_FACTORY
operator|.
name|createLinearRing
argument_list|(
name|toCoordinates
argument_list|(
name|node
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|LinearRing
index|[]
name|holes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|children
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|holes
operator|=
operator|new
name|LinearRing
index|[
name|node
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|node
operator|.
name|children
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|holes
index|[
name|i
index|]
operator|=
name|GEOMETRY_FACTORY
operator|.
name|createLinearRing
argument_list|(
name|toCoordinates
argument_list|(
name|node
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|JtsGeometry
argument_list|(
name|GEOMETRY_FACTORY
operator|.
name|createPolygon
argument_list|(
name|shell
argument_list|,
name|holes
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|true
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"multipoint"
operator|.
name|equals
argument_list|(
name|shapeType
argument_list|)
condition|)
block|{
return|return
operator|new
name|JtsGeometry
argument_list|(
name|GEOMETRY_FACTORY
operator|.
name|createMultiPoint
argument_list|(
name|toCoordinates
argument_list|(
name|node
argument_list|)
argument_list|)
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|,
literal|true
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"envelope"
operator|.
name|equals
argument_list|(
name|shapeType
argument_list|)
condition|)
block|{
name|Coordinate
index|[]
name|coordinates
init|=
name|toCoordinates
argument_list|(
name|node
argument_list|)
decl_stmt|;
return|return
operator|new
name|RectangleImpl
argument_list|(
name|coordinates
index|[
literal|0
index|]
operator|.
name|x
argument_list|,
name|coordinates
index|[
literal|1
index|]
operator|.
name|x
argument_list|,
name|coordinates
index|[
literal|1
index|]
operator|.
name|y
argument_list|,
name|coordinates
index|[
literal|0
index|]
operator|.
name|y
argument_list|,
name|GeoShapeConstants
operator|.
name|SPATIAL_CONTEXT
argument_list|)
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"ShapeType ["
operator|+
name|shapeType
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
comment|/**      * Converts the children of the given CoordinateNode into an array of      * {@link Coordinate}.      *      * @param node CoordinateNode whose children will be converted      * @return Coordinate array with the values taken from the children of the Node      */
DECL|method|toCoordinates
specifier|private
specifier|static
name|Coordinate
index|[]
name|toCoordinates
parameter_list|(
name|CoordinateNode
name|node
parameter_list|)
block|{
name|Coordinate
index|[]
name|coordinates
init|=
operator|new
name|Coordinate
index|[
name|node
operator|.
name|children
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
name|node
operator|.
name|children
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|coordinates
index|[
name|i
index|]
operator|=
name|node
operator|.
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|coordinate
expr_stmt|;
block|}
return|return
name|coordinates
return|;
block|}
comment|/**      * Node used to represent a tree of coordinates.      *<p/>      * Can either be a leaf node consisting of a Coordinate, or a parent with children      */
DECL|class|CoordinateNode
specifier|private
specifier|static
class|class
name|CoordinateNode
block|{
DECL|field|coordinate
specifier|private
name|Coordinate
name|coordinate
decl_stmt|;
DECL|field|children
specifier|private
name|List
argument_list|<
name|CoordinateNode
argument_list|>
name|children
decl_stmt|;
comment|/**          * Creates a new leaf CoordinateNode          *          * @param coordinate Coordinate for the Node          */
DECL|method|CoordinateNode
specifier|private
name|CoordinateNode
parameter_list|(
name|Coordinate
name|coordinate
parameter_list|)
block|{
name|this
operator|.
name|coordinate
operator|=
name|coordinate
expr_stmt|;
block|}
comment|/**          * Creates a new parent CoordinateNode          *          * @param children Children of the Node          */
DECL|method|CoordinateNode
specifier|private
name|CoordinateNode
parameter_list|(
name|List
argument_list|<
name|CoordinateNode
argument_list|>
name|children
parameter_list|)
block|{
name|this
operator|.
name|children
operator|=
name|children
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

