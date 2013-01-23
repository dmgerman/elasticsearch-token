begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|util
operator|.
name|BytesRef
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
name|GeoDistance
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
name|fielddata
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|MutableDateTime
import|;
end_import

begin_comment
comment|/**  * Script level doc values, the assumption is that any implementation will implement a<code>getValue</code>  * and a<code>getValues</code> that return the relevant type that then can be used in scripts.  */
end_comment

begin_interface
DECL|interface|ScriptDocValues
specifier|public
interface|interface
name|ScriptDocValues
block|{
DECL|field|EMPTY
specifier|static
specifier|final
name|ScriptDocValues
name|EMPTY
init|=
operator|new
name|Empty
argument_list|()
decl_stmt|;
DECL|field|EMPTY_STRINGS
specifier|static
specifier|final
name|Strings
name|EMPTY_STRINGS
init|=
operator|new
name|Strings
argument_list|(
name|StringValues
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|method|setNextDocId
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|isEmpty
name|boolean
name|isEmpty
parameter_list|()
function_decl|;
DECL|class|Empty
specifier|static
class|class
name|Empty
implements|implements
name|ScriptDocValues
block|{
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
DECL|class|Strings
specifier|static
class|class
name|Strings
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|StringValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|Strings
specifier|public
name|Strings
parameter_list|(
name|StringValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|String
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|StringArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|Bytes
specifier|static
class|class
name|Bytes
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|BytesValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|Bytes
specifier|public
name|Bytes
parameter_list|(
name|BytesValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|BytesRef
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|BytesRefArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericByte
specifier|static
class|class
name|NumericByte
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|ByteValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericByte
specifier|public
name|NumericByte
parameter_list|(
name|ByteValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|byte
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|ByteArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericShort
specifier|static
class|class
name|NumericShort
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|ShortValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericShort
specifier|public
name|NumericShort
parameter_list|(
name|ShortValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|short
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|ShortArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericInteger
specifier|static
class|class
name|NumericInteger
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|IntValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericInteger
specifier|public
name|NumericInteger
parameter_list|(
name|IntValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|IntArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericLong
specifier|static
class|class
name|NumericLong
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|LongValues
name|values
decl_stmt|;
DECL|field|date
specifier|private
specifier|final
name|MutableDateTime
name|date
init|=
operator|new
name|MutableDateTime
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericLong
specifier|public
name|NumericLong
parameter_list|(
name|LongValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|long
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getDate
specifier|public
name|MutableDateTime
name|getDate
parameter_list|()
block|{
name|date
operator|.
name|setMillis
argument_list|(
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|date
return|;
block|}
DECL|method|getValues
specifier|public
name|LongArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericFloat
specifier|static
class|class
name|NumericFloat
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|FloatValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericFloat
specifier|public
name|NumericFloat
parameter_list|(
name|FloatValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|float
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|FloatArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|NumericDouble
specifier|static
class|class
name|NumericDouble
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|DoubleValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|NumericDouble
specifier|public
name|NumericDouble
parameter_list|(
name|DoubleValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|double
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|DoubleArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|GeoPoints
specifier|static
class|class
name|GeoPoints
implements|implements
name|ScriptDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|GeoPointValues
name|values
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
DECL|method|GeoPoints
specifier|public
name|GeoPoints
parameter_list|(
name|GeoPointValues
name|values
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
name|GeoPoint
name|getValue
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|GeoPointArrayRef
name|getValues
parameter_list|()
block|{
return|return
name|values
operator|.
name|getValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|factorDistance
specifier|public
name|double
name|factorDistance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|FACTOR
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|factorDistanceWithDefault
specifier|public
name|double
name|factorDistanceWithDefault
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|FACTOR
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|factorDistance02
specifier|public
name|double
name|factorDistance02
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|FACTOR
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
operator|+
literal|1
return|;
block|}
DECL|method|factorDistance13
specifier|public
name|double
name|factorDistance13
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|FACTOR
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
operator|+
literal|2
return|;
block|}
DECL|method|arcDistance
specifier|public
name|double
name|arcDistance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|arcDistanceWithDefault
specifier|public
name|double
name|arcDistanceWithDefault
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|arcDistanceInKm
specifier|public
name|double
name|arcDistanceInKm
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
return|;
block|}
DECL|method|arcDistanceInKmWithDefault
specifier|public
name|double
name|arcDistanceInKmWithDefault
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|ARC
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
return|;
block|}
DECL|method|distance
specifier|public
name|double
name|distance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|distanceWithDefault
specifier|public
name|double
name|distanceWithDefault
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
return|;
block|}
DECL|method|distanceInKm
specifier|public
name|double
name|distanceInKm
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
return|;
block|}
DECL|method|distanceInKmWithDefault
specifier|public
name|double
name|distanceInKmWithDefault
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|GeoPoint
name|point
init|=
name|getValue
argument_list|()
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
return|;
block|}
block|}
block|}
end_interface

end_unit

