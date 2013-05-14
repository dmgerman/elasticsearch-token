begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.fieldcomparator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|fieldcomparator
package|;
end_package

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
name|IndexNumericFieldData
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
comment|/**  */
end_comment

begin_class
DECL|class|ShortValuesComparator
specifier|public
specifier|final
class|class
name|ShortValuesComparator
extends|extends
name|LongValuesComparatorBase
argument_list|<
name|Short
argument_list|>
block|{
DECL|field|values
specifier|private
specifier|final
name|short
index|[]
name|values
decl_stmt|;
DECL|field|sortMode
specifier|private
specifier|final
name|SortMode
name|sortMode
decl_stmt|;
DECL|method|ShortValuesComparator
specifier|public
name|ShortValuesComparator
parameter_list|(
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
parameter_list|,
name|short
name|missingValue
parameter_list|,
name|int
name|numHits
parameter_list|,
name|SortMode
name|sortMode
parameter_list|)
block|{
name|super
argument_list|(
name|indexFieldData
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|)
expr_stmt|;
assert|assert
name|indexFieldData
operator|.
name|getNumericType
argument_list|()
operator|.
name|requiredBits
argument_list|()
operator|<=
literal|16
assert|;
name|this
operator|.
name|values
operator|=
operator|new
name|short
index|[
name|numHits
index|]
expr_stmt|;
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|int
name|slot1
parameter_list|,
name|int
name|slot2
parameter_list|)
block|{
specifier|final
name|int
name|v1
init|=
name|values
index|[
name|slot1
index|]
decl_stmt|;
specifier|final
name|int
name|v2
init|=
name|values
index|[
name|slot2
index|]
decl_stmt|;
return|return
name|v1
operator|-
name|v2
return|;
comment|// we cast to int so it can't overflow
block|}
annotation|@
name|Override
DECL|method|setBottom
specifier|public
name|void
name|setBottom
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|this
operator|.
name|bottom
operator|=
name|values
index|[
name|slot
index|]
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|copy
specifier|public
name|void
name|copy
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|values
index|[
name|slot
index|]
operator|=
operator|(
name|short
operator|)
name|readerValues
operator|.
name|getValueMissing
argument_list|(
name|doc
argument_list|,
name|missingValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|Short
name|value
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
return|return
name|Short
operator|.
name|valueOf
argument_list|(
name|values
index|[
name|slot
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
block|{
name|values
index|[
name|slot
index|]
operator|+=
operator|(
name|short
operator|)
name|readerValues
operator|.
name|getValueMissing
argument_list|(
name|doc
argument_list|,
name|missingValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|divide
specifier|public
name|void
name|divide
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|divisor
parameter_list|)
block|{
name|values
index|[
name|slot
index|]
operator|/=
name|divisor
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|missing
specifier|public
name|void
name|missing
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|values
index|[
name|slot
index|]
operator|=
operator|(
name|short
operator|)
name|missingValue
expr_stmt|;
block|}
block|}
end_class

end_unit

