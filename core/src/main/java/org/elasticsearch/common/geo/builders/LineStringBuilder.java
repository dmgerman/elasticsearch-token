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

begin_class
DECL|class|LineStringBuilder
specifier|public
class|class
name|LineStringBuilder
extends|extends
name|BaseLineStringBuilder
argument_list|<
name|LineStringBuilder
argument_list|>
block|{
DECL|method|LineStringBuilder
specifier|public
name|LineStringBuilder
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
DECL|method|LineStringBuilder
specifier|public
name|LineStringBuilder
parameter_list|(
name|ArrayList
argument_list|<
name|Coordinate
argument_list|>
name|points
parameter_list|)
block|{
name|super
argument_list|(
name|points
argument_list|)
expr_stmt|;
block|}
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|GeoShapeType
name|TYPE
init|=
name|GeoShapeType
operator|.
name|LINESTRING
decl_stmt|;
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
name|shapename
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FIELD_COORDINATES
argument_list|)
expr_stmt|;
name|coordinatesToXcontent
argument_list|(
name|builder
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
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
comment|/**      * Closes the current lineString by adding the starting point as the end point      */
DECL|method|close
specifier|public
name|LineStringBuilder
name|close
parameter_list|()
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
name|points
operator|.
name|add
argument_list|(
name|start
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

