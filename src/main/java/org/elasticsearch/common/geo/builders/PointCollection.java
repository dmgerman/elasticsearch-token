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
name|Arrays
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

begin_comment
comment|/**  * The {@link PointCollection} is an abstract base implementation for all GeoShapes. It simply handles a set of points.   */
end_comment

begin_class
DECL|class|PointCollection
specifier|public
specifier|abstract
class|class
name|PointCollection
parameter_list|<
name|E
extends|extends
name|PointCollection
parameter_list|<
name|E
parameter_list|>
parameter_list|>
extends|extends
name|ShapeBuilder
block|{
DECL|field|points
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
name|points
decl_stmt|;
DECL|field|translated
specifier|protected
name|boolean
name|translated
init|=
literal|false
decl_stmt|;
DECL|method|PointCollection
specifier|protected
name|PointCollection
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|PointCollection
specifier|protected
name|PointCollection
parameter_list|(
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
name|points
parameter_list|)
block|{
name|this
operator|.
name|points
operator|=
name|points
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|thisRef
specifier|private
name|E
name|thisRef
parameter_list|()
block|{
return|return
operator|(
name|E
operator|)
name|this
return|;
block|}
comment|/**      * Add a new point to the collection      * @param longitude longitude of the coordinate      * @param latitude latitude of the coordinate      * @return this      */
DECL|method|point
specifier|public
name|E
name|point
parameter_list|(
name|double
name|longitude
parameter_list|,
name|double
name|latitude
parameter_list|)
block|{
return|return
name|this
operator|.
name|point
argument_list|(
name|coordinate
argument_list|(
name|longitude
argument_list|,
name|latitude
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Add a new point to the collection      * @param coordinate coordinate of the point      * @return this      */
DECL|method|point
specifier|public
name|E
name|point
parameter_list|(
name|Coordinate
name|coordinate
parameter_list|)
block|{
name|this
operator|.
name|points
operator|.
name|add
argument_list|(
name|coordinate
argument_list|)
expr_stmt|;
return|return
name|thisRef
argument_list|()
return|;
block|}
comment|/**      * Add a array of points to the collection      *       * @param coordinates array of {@link Coordinate}s to add      * @return this      */
DECL|method|points
specifier|public
name|E
name|points
parameter_list|(
name|Coordinate
modifier|...
name|coordinates
parameter_list|)
block|{
return|return
name|this
operator|.
name|points
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|coordinates
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Add a collection of points to the collection      *       * @param coordinates array of {@link Coordinate}s to add      * @return this      */
DECL|method|points
specifier|public
name|E
name|points
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Coordinate
argument_list|>
name|coordinates
parameter_list|)
block|{
name|this
operator|.
name|points
operator|.
name|addAll
argument_list|(
name|coordinates
argument_list|)
expr_stmt|;
return|return
name|thisRef
argument_list|()
return|;
block|}
comment|/**      * Copy all points to a new Array      *       * @param closed if set to true the first point of the array is repeated as last element      * @return Array of coordinates      */
DECL|method|coordinates
specifier|protected
name|Coordinate
index|[]
name|coordinates
parameter_list|(
name|boolean
name|closed
parameter_list|)
block|{
name|Coordinate
index|[]
name|result
init|=
name|points
operator|.
name|toArray
argument_list|(
operator|new
name|Coordinate
index|[
name|points
operator|.
name|size
argument_list|()
operator|+
operator|(
name|closed
condition|?
literal|1
else|:
literal|0
operator|)
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|closed
condition|)
block|{
name|result
index|[
name|result
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|result
index|[
literal|0
index|]
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * builds an array of coordinates to a {@link XContentBuilder}      *       * @param builder builder to use       * @param closed repeat the first point at the end of the array if it's not already defines as last element of the array        * @return the builder      * @throws IOException      */
DECL|method|coordinatesToXcontent
specifier|protected
name|XContentBuilder
name|coordinatesToXcontent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|boolean
name|closed
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Coordinate
name|point
range|:
name|points
control|)
block|{
name|toXContent
argument_list|(
name|builder
argument_list|,
name|point
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|closed
condition|)
block|{
name|Coordinate
name|start
init|=
name|points
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Coordinate
name|end
init|=
name|points
operator|.
name|get
argument_list|(
name|points
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|start
operator|.
name|x
operator|!=
name|end
operator|.
name|x
operator|||
name|start
operator|.
name|y
operator|!=
name|end
operator|.
name|y
condition|)
block|{
name|toXContent
argument_list|(
name|builder
argument_list|,
name|points
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

