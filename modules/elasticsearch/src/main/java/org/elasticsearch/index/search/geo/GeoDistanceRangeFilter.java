begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|NumericUtils
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
name|common
operator|.
name|unit
operator|.
name|DistanceUnit
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
DECL|class|GeoDistanceRangeFilter
specifier|public
class|class
name|GeoDistanceRangeFilter
extends|extends
name|Filter
block|{
DECL|field|lat
specifier|private
specifier|final
name|double
name|lat
decl_stmt|;
DECL|field|lon
specifier|private
specifier|final
name|double
name|lon
decl_stmt|;
DECL|field|inclusiveLowerPoint
specifier|private
specifier|final
name|double
name|inclusiveLowerPoint
decl_stmt|;
comment|// in miles
DECL|field|inclusiveUpperPoint
specifier|private
specifier|final
name|double
name|inclusiveUpperPoint
decl_stmt|;
comment|// in miles
DECL|field|geoDistance
specifier|private
specifier|final
name|GeoDistance
name|geoDistance
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
DECL|method|GeoDistanceRangeFilter
specifier|public
name|GeoDistanceRangeFilter
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|Double
name|lowerVal
parameter_list|,
name|Double
name|upperVal
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|,
name|GeoDistance
name|geoDistance
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
name|this
operator|.
name|geoDistance
operator|=
name|geoDistance
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
if|if
condition|(
name|lowerVal
operator|!=
literal|null
condition|)
block|{
name|double
name|f
init|=
name|lowerVal
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|long
name|i
init|=
name|NumericUtils
operator|.
name|doubleToSortableLong
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|inclusiveLowerPoint
operator|=
name|NumericUtils
operator|.
name|sortableLongToDouble
argument_list|(
name|includeLower
condition|?
name|i
else|:
operator|(
name|i
operator|+
literal|1L
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inclusiveLowerPoint
operator|=
name|Double
operator|.
name|NEGATIVE_INFINITY
expr_stmt|;
block|}
if|if
condition|(
name|upperVal
operator|!=
literal|null
condition|)
block|{
name|double
name|f
init|=
name|upperVal
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|long
name|i
init|=
name|NumericUtils
operator|.
name|doubleToSortableLong
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|inclusiveUpperPoint
operator|=
name|NumericUtils
operator|.
name|sortableLongToDouble
argument_list|(
name|includeUpper
condition|?
name|i
else|:
operator|(
name|i
operator|-
literal|1L
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inclusiveUpperPoint
operator|=
name|Double
operator|.
name|POSITIVE_INFINITY
expr_stmt|;
block|}
block|}
DECL|method|lat
specifier|public
name|double
name|lat
parameter_list|()
block|{
return|return
name|lat
return|;
block|}
DECL|method|lon
specifier|public
name|double
name|lon
parameter_list|()
block|{
return|return
name|lon
return|;
block|}
DECL|method|geoDistance
specifier|public
name|GeoDistance
name|geoDistance
parameter_list|()
block|{
return|return
name|geoDistance
return|;
block|}
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
name|isCacheable
parameter_list|()
block|{
comment|// not cacheable for several reasons:
comment|// 1. It is only relevant when _cache is set to true, and then, we really want to create in mem bitset
comment|// 2. Its already fast without in mem bitset, since it works with field data
return|return
literal|false
return|;
block|}
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
name|double
index|[]
name|lats
init|=
name|fieldData
operator|.
name|latValues
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|double
index|[]
name|lons
init|=
name|fieldData
operator|.
name|lonValues
argument_list|(
name|doc
argument_list|)
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
name|lats
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|double
name|d
init|=
name|geoDistance
operator|.
name|calculate
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|lats
index|[
name|i
index|]
argument_list|,
name|lons
index|[
name|i
index|]
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
decl_stmt|;
if|if
condition|(
name|d
operator|>=
name|inclusiveLowerPoint
operator|&&
name|d
operator|<=
name|inclusiveUpperPoint
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
else|else
block|{
name|double
name|d
init|=
name|geoDistance
operator|.
name|calculate
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|fieldData
operator|.
name|latValue
argument_list|(
name|doc
argument_list|)
argument_list|,
name|fieldData
operator|.
name|lonValue
argument_list|(
name|doc
argument_list|)
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
decl_stmt|;
if|if
condition|(
name|d
operator|>=
name|inclusiveLowerPoint
operator|&&
name|d
operator|<=
name|inclusiveUpperPoint
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|GeoDistanceRangeFilter
name|filter
init|=
operator|(
name|GeoDistanceRangeFilter
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|inclusiveLowerPoint
argument_list|,
name|inclusiveLowerPoint
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|inclusiveUpperPoint
argument_list|,
name|inclusiveUpperPoint
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|lat
argument_list|,
name|lat
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|filter
operator|.
name|lon
argument_list|,
name|lon
argument_list|)
operator|!=
literal|0
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|fieldName
operator|!=
literal|null
condition|?
operator|!
name|fieldName
operator|.
name|equals
argument_list|(
name|filter
operator|.
name|fieldName
argument_list|)
else|:
name|filter
operator|.
name|fieldName
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|geoDistance
operator|!=
name|filter
operator|.
name|geoDistance
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|int
name|result
decl_stmt|;
name|long
name|temp
decl_stmt|;
name|temp
operator|=
name|lat
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|lat
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
call|(
name|int
call|)
argument_list|(
name|temp
operator|^
operator|(
name|temp
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|temp
operator|=
name|lon
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|lon
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|temp
operator|^
operator|(
name|temp
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|temp
operator|=
name|inclusiveLowerPoint
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|inclusiveLowerPoint
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|temp
operator|^
operator|(
name|temp
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|temp
operator|=
name|inclusiveUpperPoint
operator|!=
operator|+
literal|0.0d
condition|?
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|inclusiveUpperPoint
argument_list|)
else|:
literal|0L
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|temp
operator|^
operator|(
name|temp
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|geoDistance
operator|!=
literal|null
condition|?
name|geoDistance
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|fieldName
operator|!=
literal|null
condition|?
name|fieldName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

