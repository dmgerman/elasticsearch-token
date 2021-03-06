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
name|org
operator|.
name|locationtech
operator|.
name|spatial4j
operator|.
name|shape
operator|.
name|Point
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

begin_class
DECL|class|PointBuilder
specifier|public
class|class
name|PointBuilder
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
name|POINT
decl_stmt|;
DECL|field|coordinate
specifier|private
name|Coordinate
name|coordinate
decl_stmt|;
comment|/**      * Create a point at [0.0,0.0]      */
DECL|method|PointBuilder
specifier|public
name|PointBuilder
parameter_list|()
block|{
name|this
operator|.
name|coordinate
operator|=
name|ZERO_ZERO
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|PointBuilder
specifier|public
name|PointBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|coordinate
operator|=
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
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
name|writeCoordinateTo
argument_list|(
name|coordinate
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|coordinate
specifier|public
name|PointBuilder
name|coordinate
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
return|return
name|this
return|;
block|}
DECL|method|longitude
specifier|public
name|double
name|longitude
parameter_list|()
block|{
return|return
name|coordinate
operator|.
name|x
return|;
block|}
DECL|method|latitude
specifier|public
name|double
name|latitude
parameter_list|()
block|{
return|return
name|coordinate
operator|.
name|y
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
name|field
argument_list|(
name|FIELD_COORDINATES
argument_list|)
expr_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|coordinate
argument_list|)
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
DECL|method|build
specifier|public
name|Point
name|build
parameter_list|()
block|{
return|return
name|SPATIAL_CONTEXT
operator|.
name|makePoint
argument_list|(
name|coordinate
operator|.
name|x
argument_list|,
name|coordinate
operator|.
name|y
argument_list|)
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
name|coordinate
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
name|PointBuilder
name|other
init|=
operator|(
name|PointBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|coordinate
argument_list|,
name|other
operator|.
name|coordinate
argument_list|)
return|;
block|}
block|}
end_class

end_unit

