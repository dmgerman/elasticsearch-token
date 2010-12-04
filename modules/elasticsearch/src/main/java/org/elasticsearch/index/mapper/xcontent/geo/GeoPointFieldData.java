begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent.geo
package|package
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
name|elasticsearch
operator|.
name|common
operator|.
name|thread
operator|.
name|ThreadLocals
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
name|trove
operator|.
name|TDoubleArrayList
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|field
operator|.
name|data
operator|.
name|support
operator|.
name|FieldDataLoader
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
name|search
operator|.
name|geo
operator|.
name|GeoHashUtils
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
DECL|class|GeoPointFieldData
specifier|public
specifier|abstract
class|class
name|GeoPointFieldData
extends|extends
name|FieldData
argument_list|<
name|GeoPointDocFieldData
argument_list|>
block|{
DECL|field|valuesCache
specifier|static
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|GeoPoint
argument_list|>
argument_list|>
name|valuesCache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|GeoPoint
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|GeoPoint
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|GeoPoint
argument_list|>
argument_list|(
operator|new
name|GeoPoint
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|EMPTY_ARRAY
specifier|public
specifier|static
specifier|final
name|GeoPoint
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|GeoPoint
index|[
literal|0
index|]
decl_stmt|;
DECL|field|lat
specifier|protected
specifier|final
name|double
index|[]
name|lat
decl_stmt|;
DECL|field|lon
specifier|protected
specifier|final
name|double
index|[]
name|lon
decl_stmt|;
DECL|method|GeoPointFieldData
specifier|protected
name|GeoPointFieldData
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|double
index|[]
name|lat
parameter_list|,
name|double
index|[]
name|lon
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
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
DECL|method|value
specifier|abstract
specifier|public
name|GeoPoint
name|value
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|values
specifier|abstract
specifier|public
name|GeoPoint
index|[]
name|values
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|latValue
specifier|abstract
specifier|public
name|double
name|latValue
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|lonValue
specifier|abstract
specifier|public
name|double
name|lonValue
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|latValues
specifier|abstract
specifier|public
name|double
index|[]
name|latValues
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|lonValues
specifier|abstract
specifier|public
name|double
index|[]
name|lonValues
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|docFieldData
annotation|@
name|Override
specifier|public
name|GeoPointDocFieldData
name|docFieldData
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|super
operator|.
name|docFieldData
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|stringValue
annotation|@
name|Override
specifier|public
name|String
name|stringValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|docId
argument_list|)
operator|.
name|geohash
argument_list|()
return|;
block|}
DECL|method|createFieldData
annotation|@
name|Override
specifier|protected
name|GeoPointDocFieldData
name|createFieldData
parameter_list|()
block|{
return|return
operator|new
name|GeoPointDocFieldData
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|FieldDataType
name|type
parameter_list|()
block|{
return|return
name|GeoPointFieldDataType
operator|.
name|TYPE
return|;
block|}
DECL|method|forEachValue
annotation|@
name|Override
specifier|public
name|void
name|forEachValue
parameter_list|(
name|StringValueProc
name|proc
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|lat
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|proc
operator|.
name|onValue
argument_list|(
name|GeoHashUtils
operator|.
name|encode
argument_list|(
name|lat
index|[
name|i
index|]
argument_list|,
name|lon
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|forEachValue
specifier|public
name|void
name|forEachValue
parameter_list|(
name|PointValueProc
name|proc
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|lat
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|GeoPoint
name|point
init|=
name|valuesCache
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|point
operator|.
name|latlon
argument_list|(
name|lat
index|[
name|i
index|]
argument_list|,
name|lon
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|proc
operator|.
name|onValue
argument_list|(
name|point
argument_list|)
expr_stmt|;
block|}
block|}
DECL|interface|PointValueProc
specifier|public
specifier|static
interface|interface
name|PointValueProc
block|{
DECL|method|onValue
name|void
name|onValue
parameter_list|(
name|GeoPoint
name|value
parameter_list|)
function_decl|;
block|}
DECL|method|forEachValue
specifier|public
name|void
name|forEachValue
parameter_list|(
name|ValueProc
name|proc
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|lat
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|proc
operator|.
name|onValue
argument_list|(
name|lat
index|[
name|i
index|]
argument_list|,
name|lon
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
DECL|interface|ValueProc
specifier|public
specifier|static
interface|interface
name|ValueProc
block|{
DECL|method|onValue
name|void
name|onValue
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
function_decl|;
block|}
DECL|method|load
specifier|public
specifier|static
name|GeoPointFieldData
name|load
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|FieldDataLoader
operator|.
name|load
argument_list|(
name|reader
argument_list|,
name|field
argument_list|,
operator|new
name|StringTypeLoader
argument_list|()
argument_list|)
return|;
block|}
DECL|class|StringTypeLoader
specifier|static
class|class
name|StringTypeLoader
extends|extends
name|FieldDataLoader
operator|.
name|FreqsTypeLoader
argument_list|<
name|GeoPointFieldData
argument_list|>
block|{
DECL|field|lat
specifier|private
specifier|final
name|TDoubleArrayList
name|lat
init|=
operator|new
name|TDoubleArrayList
argument_list|()
decl_stmt|;
DECL|field|lon
specifier|private
specifier|final
name|TDoubleArrayList
name|lon
init|=
operator|new
name|TDoubleArrayList
argument_list|()
decl_stmt|;
DECL|method|StringTypeLoader
name|StringTypeLoader
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// the first one indicates null value
name|lat
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|lon
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|collectTerm
annotation|@
name|Override
specifier|public
name|void
name|collectTerm
parameter_list|(
name|String
name|term
parameter_list|)
block|{
name|int
name|comma
init|=
name|term
operator|.
name|indexOf
argument_list|(
literal|','
argument_list|)
decl_stmt|;
name|lat
operator|.
name|add
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|term
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|comma
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|lon
operator|.
name|add
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|term
operator|.
name|substring
argument_list|(
name|comma
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildSingleValue
annotation|@
name|Override
specifier|public
name|GeoPointFieldData
name|buildSingleValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
name|ordinals
parameter_list|)
block|{
return|return
operator|new
name|SingleValueGeoPointFieldData
argument_list|(
name|field
argument_list|,
name|ordinals
argument_list|,
name|lat
operator|.
name|toNativeArray
argument_list|()
argument_list|,
name|lon
operator|.
name|toNativeArray
argument_list|()
argument_list|)
return|;
block|}
DECL|method|buildMultiValue
annotation|@
name|Override
specifier|public
name|GeoPointFieldData
name|buildMultiValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
index|[]
name|ordinals
parameter_list|)
block|{
return|return
operator|new
name|MultiValueGeoPointFieldData
argument_list|(
name|field
argument_list|,
name|ordinals
argument_list|,
name|lat
operator|.
name|toNativeArray
argument_list|()
argument_list|,
name|lon
operator|.
name|toNativeArray
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

