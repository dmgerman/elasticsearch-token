begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|geo
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
name|IndexReader
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
name|DocIdSet
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
name|Filter
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
name|docset
operator|.
name|GetDocSet
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
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|xcontent
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
name|index
operator|.
name|mapper
operator|.
name|xcontent
operator|.
name|geo
operator|.
name|GeoPointFieldData
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
name|xcontent
operator|.
name|geo
operator|.
name|GeoPointFieldDataType
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoPolygonFilter
specifier|public
class|class
name|GeoPolygonFilter
extends|extends
name|Filter
block|{
DECL|field|points
specifier|private
specifier|final
name|Point
index|[]
name|points
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
decl_stmt|;
DECL|method|GeoPolygonFilter
specifier|public
name|GeoPolygonFilter
parameter_list|(
name|Point
index|[]
name|points
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|FieldDataCache
name|fieldDataCache
parameter_list|)
block|{
name|this
operator|.
name|points
operator|=
name|points
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|fieldDataCache
operator|=
name|fieldDataCache
expr_stmt|;
block|}
DECL|method|points
specifier|public
name|Point
index|[]
name|points
parameter_list|()
block|{
return|return
name|points
return|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
DECL|method|getDocIdSet
annotation|@
name|Override
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|GeoPointFieldData
name|fieldData
init|=
operator|(
name|GeoPointFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|GeoPointFieldDataType
operator|.
name|TYPE
argument_list|,
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
return|return
operator|new
name|GetDocSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fieldData
operator|.
name|hasValue
argument_list|(
name|doc
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|fieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
name|GeoPoint
index|[]
name|docPoints
init|=
name|fieldData
operator|.
name|values
argument_list|(
name|doc
argument_list|)
decl_stmt|;
for|for
control|(
name|GeoPoint
name|docPoint
range|:
name|docPoints
control|)
block|{
if|if
condition|(
name|pointInPolygon
argument_list|(
name|points
argument_list|,
name|docPoint
operator|.
name|lat
argument_list|()
argument_list|,
name|docPoint
operator|.
name|lon
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
else|else
block|{
name|GeoPoint
name|point
init|=
name|fieldData
operator|.
name|value
argument_list|(
name|doc
argument_list|)
decl_stmt|;
return|return
name|pointInPolygon
argument_list|(
name|points
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
DECL|method|pointInPolygon
specifier|private
specifier|static
name|boolean
name|pointInPolygon
parameter_list|(
name|Point
index|[]
name|points
parameter_list|,
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|int
name|i
decl_stmt|;
name|int
name|j
init|=
name|points
operator|.
name|length
operator|-
literal|1
decl_stmt|;
name|boolean
name|inPoly
init|=
literal|false
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|points
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|points
index|[
name|i
index|]
operator|.
name|lon
operator|<
name|lon
operator|&&
name|points
index|[
name|j
index|]
operator|.
name|lon
operator|>=
name|lon
operator|||
name|points
index|[
name|j
index|]
operator|.
name|lon
operator|<
name|lon
operator|&&
name|points
index|[
name|i
index|]
operator|.
name|lon
operator|>=
name|lon
condition|)
block|{
if|if
condition|(
name|points
index|[
name|i
index|]
operator|.
name|lat
operator|+
operator|(
name|lon
operator|-
name|points
index|[
name|i
index|]
operator|.
name|lon
operator|)
operator|/
operator|(
name|points
index|[
name|j
index|]
operator|.
name|lon
operator|-
name|points
index|[
name|i
index|]
operator|.
name|lon
operator|)
operator|*
operator|(
name|points
index|[
name|j
index|]
operator|.
name|lat
operator|-
name|points
index|[
name|i
index|]
operator|.
name|lat
operator|)
operator|<
name|lat
condition|)
block|{
name|inPoly
operator|=
operator|!
name|inPoly
expr_stmt|;
block|}
block|}
name|j
operator|=
name|i
expr_stmt|;
block|}
return|return
name|inPoly
return|;
block|}
DECL|class|Point
specifier|public
specifier|static
class|class
name|Point
block|{
DECL|field|lat
specifier|public
name|double
name|lat
decl_stmt|;
DECL|field|lon
specifier|public
name|double
name|lon
decl_stmt|;
DECL|method|Point
specifier|public
name|Point
parameter_list|()
block|{         }
DECL|method|Point
specifier|public
name|Point
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

