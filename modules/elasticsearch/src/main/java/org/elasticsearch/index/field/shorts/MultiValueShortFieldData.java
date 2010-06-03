begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.field.shorts
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|shorts
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
name|field
operator|.
name|FieldDataOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|ThreadLocals
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|MultiValueShortFieldData
specifier|public
class|class
name|MultiValueShortFieldData
extends|extends
name|ShortFieldData
block|{
DECL|field|VALUE_CACHE_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|VALUE_CACHE_SIZE
init|=
literal|100
decl_stmt|;
DECL|field|valuesCache
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|short
index|[]
index|[]
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
name|short
index|[]
index|[]
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
name|short
index|[]
index|[]
argument_list|>
name|initialValue
parameter_list|()
block|{
name|short
index|[]
index|[]
name|value
init|=
operator|new
name|short
index|[
name|VALUE_CACHE_SIZE
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
name|value
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|i
index|]
operator|=
operator|new
name|short
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|short
index|[]
index|[]
argument_list|>
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// order with value 0 indicates no value
DECL|field|order
specifier|private
specifier|final
name|int
index|[]
index|[]
name|order
decl_stmt|;
DECL|method|MultiValueShortFieldData
specifier|public
name|MultiValueShortFieldData
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|FieldDataOptions
name|options
parameter_list|,
name|int
index|[]
index|[]
name|order
parameter_list|,
name|short
index|[]
name|values
parameter_list|,
name|int
index|[]
name|freqs
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|options
argument_list|,
name|values
argument_list|,
name|freqs
argument_list|)
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
block|}
DECL|method|multiValued
annotation|@
name|Override
specifier|public
name|boolean
name|multiValued
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|hasValue
annotation|@
name|Override
specifier|public
name|boolean
name|hasValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|order
index|[
name|docId
index|]
operator|!=
literal|null
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|short
name|value
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|int
index|[]
name|docOrders
init|=
name|order
index|[
name|docId
index|]
decl_stmt|;
if|if
condition|(
name|docOrders
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|values
index|[
name|docOrders
index|[
literal|0
index|]
index|]
return|;
block|}
DECL|method|values
annotation|@
name|Override
specifier|public
name|short
index|[]
name|values
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|int
index|[]
name|docOrders
init|=
name|order
index|[
name|docId
index|]
decl_stmt|;
if|if
condition|(
name|docOrders
operator|==
literal|null
condition|)
block|{
return|return
name|EMPTY_SHORT_ARRAY
return|;
block|}
name|short
index|[]
name|shorts
decl_stmt|;
if|if
condition|(
name|docOrders
operator|.
name|length
operator|<
name|VALUE_CACHE_SIZE
condition|)
block|{
name|shorts
operator|=
name|valuesCache
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
index|[
name|docOrders
operator|.
name|length
index|]
expr_stmt|;
block|}
else|else
block|{
name|shorts
operator|=
operator|new
name|short
index|[
name|docOrders
operator|.
name|length
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docOrders
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|shorts
index|[
name|i
index|]
operator|=
name|values
index|[
name|docOrders
index|[
name|i
index|]
index|]
expr_stmt|;
block|}
return|return
name|shorts
return|;
block|}
block|}
end_class

end_unit

