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
name|NumericDocValues
import|;
end_import

begin_comment
comment|/**  * A per-document numeric value.  */
end_comment

begin_class
DECL|class|NumericDoubleValues
specifier|public
specifier|abstract
class|class
name|NumericDoubleValues
block|{
comment|/** Sole constructor. (For invocation by subclass    * constructors, typically implicit.) */
DECL|method|NumericDoubleValues
specifier|protected
name|NumericDoubleValues
parameter_list|()
block|{}
comment|/**    * Returns the numeric value for the specified document ID. This must return    *<tt>0d</tt> if the given doc ID has no value.    * @param docID document ID to lookup    * @return numeric value    */
DECL|method|get
specifier|public
specifier|abstract
name|double
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
function_decl|;
comment|// TODO: this interaction with sort comparators is really ugly...
comment|/** Returns numeric docvalues view of raw double bits */
DECL|method|getRawDoubleValues
specifier|public
name|NumericDocValues
name|getRawDoubleValues
parameter_list|()
block|{
return|return
operator|new
name|NumericDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
return|return
name|Double
operator|.
name|doubleToRawLongBits
argument_list|(
name|NumericDoubleValues
operator|.
name|this
operator|.
name|get
argument_list|(
name|docID
argument_list|)
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|// yes... this is doing what the previous code was doing...
comment|/** Returns numeric docvalues view of raw float bits */
DECL|method|getRawFloatValues
specifier|public
name|NumericDocValues
name|getRawFloatValues
parameter_list|()
block|{
return|return
operator|new
name|NumericDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
return|return
name|Float
operator|.
name|floatToRawIntBits
argument_list|(
operator|(
name|float
operator|)
name|NumericDoubleValues
operator|.
name|this
operator|.
name|get
argument_list|(
name|docID
argument_list|)
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

