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
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoHashUtils
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

begin_class
DECL|class|GeoPolygonQueryBuilder
specifier|public
class|class
name|GeoPolygonQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|GeoPolygonQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_polygon"
decl_stmt|;
DECL|field|POINTS
specifier|public
specifier|static
specifier|final
name|String
name|POINTS
init|=
name|GeoPolygonQueryParser
operator|.
name|POINTS
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|shell
specifier|private
specifier|final
name|List
argument_list|<
name|GeoPoint
argument_list|>
name|shell
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|GeoPolygonQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|GeoPolygonQueryBuilder
argument_list|(
literal|null
argument_list|)
decl_stmt|;
DECL|field|coerce
specifier|private
name|Boolean
name|coerce
decl_stmt|;
DECL|field|ignoreMalformed
specifier|private
name|Boolean
name|ignoreMalformed
decl_stmt|;
DECL|method|GeoPolygonQueryBuilder
specifier|public
name|GeoPolygonQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * Adds a point with lat and lon      *      * @param lat The latitude      * @param lon The longitude      * @return the current builder      */
DECL|method|addPoint
specifier|public
name|GeoPolygonQueryBuilder
name|addPoint
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|addPoint
argument_list|(
operator|new
name|GeoPoint
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
argument_list|)
return|;
block|}
DECL|method|addPoint
specifier|public
name|GeoPolygonQueryBuilder
name|addPoint
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
return|return
name|addPoint
argument_list|(
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|geohash
argument_list|)
argument_list|)
return|;
block|}
DECL|method|addPoint
specifier|public
name|GeoPolygonQueryBuilder
name|addPoint
parameter_list|(
name|GeoPoint
name|point
parameter_list|)
block|{
name|shell
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
DECL|method|coerce
specifier|public
name|GeoPolygonQueryBuilder
name|coerce
parameter_list|(
name|boolean
name|coerce
parameter_list|)
block|{
name|this
operator|.
name|coerce
operator|=
name|coerce
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ignoreMalformed
specifier|public
name|GeoPolygonQueryBuilder
name|ignoreMalformed
parameter_list|(
name|boolean
name|ignoreMalformed
parameter_list|)
block|{
name|this
operator|.
name|ignoreMalformed
operator|=
name|ignoreMalformed
expr_stmt|;
return|return
name|this
return|;
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
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|POINTS
argument_list|)
expr_stmt|;
for|for
control|(
name|GeoPoint
name|point
range|:
name|shell
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
name|point
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
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
if|if
condition|(
name|coerce
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"coerce"
argument_list|,
name|coerce
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ignoreMalformed
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_malformed"
argument_list|,
name|ignoreMalformed
argument_list|)
expr_stmt|;
block|}
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

