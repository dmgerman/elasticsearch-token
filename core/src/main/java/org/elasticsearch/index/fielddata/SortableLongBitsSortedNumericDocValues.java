begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|index
operator|.
name|SortedNumericDocValues
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

begin_comment
comment|/**  * {@link SortedNumericDocValues} instance that wraps a {@link SortedNumericDoubleValues}  * and converts the doubles to sortable long bits using  * {@link NumericUtils#doubleToSortableLong(double)}.  */
end_comment

begin_class
DECL|class|SortableLongBitsSortedNumericDocValues
specifier|final
class|class
name|SortableLongBitsSortedNumericDocValues
extends|extends
name|SortedNumericDocValues
block|{
DECL|field|values
specifier|private
specifier|final
name|SortedNumericDoubleValues
name|values
decl_stmt|;
DECL|method|SortableLongBitsSortedNumericDocValues
name|SortableLongBitsSortedNumericDocValues
parameter_list|(
name|SortedNumericDoubleValues
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
DECL|method|setDocument
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|valueAt
specifier|public
name|long
name|valueAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|NumericUtils
operator|.
name|doubleToSortableLong
argument_list|(
name|values
operator|.
name|valueAt
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|count
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|values
operator|.
name|count
argument_list|()
return|;
block|}
comment|/** Return the wrapped values. */
DECL|method|getDoubleValues
specifier|public
name|SortedNumericDoubleValues
name|getDoubleValues
parameter_list|()
block|{
return|return
name|values
return|;
block|}
block|}
end_class

end_unit
