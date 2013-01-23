begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
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
name|AtomicReaderContext
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Bits
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
name|Nullable
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
name|lucene
operator|.
name|docset
operator|.
name|MatchDocIdSet
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
name|fielddata
operator|.
name|GeoPointValues
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
name|fielddata
operator|.
name|IndexGeoPointFieldData
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
name|Arrays
import|;
end_import

begin_comment
comment|/**  *  */
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
name|GeoPoint
index|[]
name|points
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexGeoPointFieldData
name|indexFieldData
decl_stmt|;
DECL|method|GeoPolygonFilter
specifier|public
name|GeoPolygonFilter
parameter_list|(
name|GeoPoint
index|[]
name|points
parameter_list|,
name|IndexGeoPointFieldData
name|indexFieldData
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
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
block|}
DECL|method|points
specifier|public
name|GeoPoint
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
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|Bits
name|acceptedDocs
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|GeoPointValues
name|values
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getGeoPointValues
argument_list|()
decl_stmt|;
return|return
operator|new
name|GeoPolygonDocIdSet
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|acceptedDocs
argument_list|,
name|values
argument_list|,
name|points
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"GeoPolygonFilter("
operator|+
name|indexFieldData
operator|.
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
operator|+
literal|", "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|points
argument_list|)
operator|+
literal|")"
return|;
block|}
DECL|class|GeoPolygonDocIdSet
specifier|public
specifier|static
class|class
name|GeoPolygonDocIdSet
extends|extends
name|MatchDocIdSet
block|{
DECL|field|values
specifier|private
specifier|final
name|GeoPointValues
name|values
decl_stmt|;
DECL|field|points
specifier|private
specifier|final
name|GeoPoint
index|[]
name|points
decl_stmt|;
DECL|method|GeoPolygonDocIdSet
specifier|public
name|GeoPolygonDocIdSet
parameter_list|(
name|int
name|maxDoc
parameter_list|,
annotation|@
name|Nullable
name|Bits
name|acceptDocs
parameter_list|,
name|GeoPointValues
name|values
parameter_list|,
name|GeoPoint
index|[]
name|points
parameter_list|)
block|{
name|super
argument_list|(
name|maxDoc
argument_list|,
name|acceptDocs
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|points
operator|=
name|points
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isCacheable
specifier|public
name|boolean
name|isCacheable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|matchDoc
specifier|protected
name|boolean
name|matchDoc
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
if|if
condition|(
operator|!
name|values
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
name|values
operator|.
name|isMultiValued
argument_list|()
condition|)
block|{
name|GeoPointValues
operator|.
name|Iter
name|iter
init|=
name|values
operator|.
name|getIter
argument_list|(
name|doc
argument_list|)
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|GeoPoint
name|point
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
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
name|values
operator|.
name|getValue
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
DECL|method|pointInPolygon
specifier|private
specifier|static
name|boolean
name|pointInPolygon
parameter_list|(
name|GeoPoint
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
argument_list|()
operator|<
name|lon
operator|&&
name|points
index|[
name|j
index|]
operator|.
name|lon
argument_list|()
operator|>=
name|lon
operator|||
name|points
index|[
name|j
index|]
operator|.
name|lon
argument_list|()
operator|<
name|lon
operator|&&
name|points
index|[
name|i
index|]
operator|.
name|lon
argument_list|()
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
argument_list|()
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
argument_list|()
operator|)
operator|/
operator|(
name|points
index|[
name|j
index|]
operator|.
name|lon
argument_list|()
operator|-
name|points
index|[
name|i
index|]
operator|.
name|lon
argument_list|()
operator|)
operator|*
operator|(
name|points
index|[
name|j
index|]
operator|.
name|lat
argument_list|()
operator|-
name|points
index|[
name|i
index|]
operator|.
name|lat
argument_list|()
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
block|}
block|}
end_class

end_unit

