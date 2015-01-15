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
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoPoint
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
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|Geometry
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
name|LineString
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
name|Iterator
import|;
end_import

begin_class
DECL|class|MultiLineStringBuilder
specifier|public
class|class
name|MultiLineStringBuilder
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
name|MULTILINESTRING
decl_stmt|;
DECL|field|lines
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|BaseLineStringBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|lines
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|linestring
specifier|public
name|InternalLineStringBuilder
name|linestring
parameter_list|()
block|{
name|InternalLineStringBuilder
name|line
init|=
operator|new
name|InternalLineStringBuilder
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|this
operator|.
name|lines
operator|.
name|add
argument_list|(
name|line
argument_list|)
expr_stmt|;
return|return
name|line
return|;
block|}
DECL|method|linestring
specifier|public
name|MultiLineStringBuilder
name|linestring
parameter_list|(
name|BaseLineStringBuilder
argument_list|<
name|?
argument_list|>
name|line
parameter_list|)
block|{
name|this
operator|.
name|lines
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
DECL|method|coordinates
specifier|public
name|GeoPoint
index|[]
index|[]
name|coordinates
parameter_list|()
block|{
name|GeoPoint
index|[]
index|[]
name|result
init|=
operator|new
name|GeoPoint
index|[
name|lines
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|result
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|lines
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|coordinates
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|result
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
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|BaseLineStringBuilder
argument_list|<
name|?
argument_list|>
name|line
range|:
name|lines
control|)
block|{
name|line
operator|.
name|coordinatesToXcontent
argument_list|(
name|builder
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
DECL|method|build
specifier|public
name|Shape
name|build
parameter_list|()
block|{
specifier|final
name|Geometry
name|geometry
decl_stmt|;
if|if
condition|(
name|wrapdateline
condition|)
block|{
name|ArrayList
argument_list|<
name|LineString
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseLineStringBuilder
argument_list|<
name|?
argument_list|>
name|line
range|:
name|lines
control|)
block|{
name|BaseLineStringBuilder
operator|.
name|decompose
argument_list|(
name|FACTORY
argument_list|,
name|line
operator|.
name|coordinates
argument_list|(
literal|false
argument_list|)
argument_list|,
name|parts
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|parts
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|geometry
operator|=
name|parts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LineString
index|[]
name|lineStrings
init|=
name|parts
operator|.
name|toArray
argument_list|(
operator|new
name|LineString
index|[
name|parts
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|geometry
operator|=
name|FACTORY
operator|.
name|createMultiLineString
argument_list|(
name|lineStrings
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LineString
index|[]
name|lineStrings
init|=
operator|new
name|LineString
index|[
name|lines
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|Iterator
argument_list|<
name|BaseLineStringBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|iterator
init|=
name|lines
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|lineStrings
index|[
name|i
index|]
operator|=
name|FACTORY
operator|.
name|createLineString
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|coordinates
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|geometry
operator|=
name|FACTORY
operator|.
name|createMultiLineString
argument_list|(
name|lineStrings
argument_list|)
expr_stmt|;
block|}
return|return
name|jtsGeometry
argument_list|(
name|geometry
argument_list|)
return|;
block|}
DECL|class|InternalLineStringBuilder
specifier|public
specifier|static
class|class
name|InternalLineStringBuilder
extends|extends
name|BaseLineStringBuilder
argument_list|<
name|InternalLineStringBuilder
argument_list|>
block|{
DECL|field|collection
specifier|private
specifier|final
name|MultiLineStringBuilder
name|collection
decl_stmt|;
DECL|method|InternalLineStringBuilder
specifier|public
name|InternalLineStringBuilder
parameter_list|(
name|MultiLineStringBuilder
name|collection
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|collection
operator|=
name|collection
expr_stmt|;
block|}
DECL|method|end
specifier|public
name|MultiLineStringBuilder
name|end
parameter_list|()
block|{
return|return
name|collection
return|;
block|}
DECL|method|coordinates
specifier|public
name|GeoPoint
index|[]
name|coordinates
parameter_list|()
block|{
return|return
name|super
operator|.
name|coordinates
argument_list|(
literal|false
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
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

