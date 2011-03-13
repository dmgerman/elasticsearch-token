begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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

begin_comment
comment|/**  * A sort builder to sort based on a document field.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FieldSortBuilder
specifier|public
class|class
name|FieldSortBuilder
extends|extends
name|SortBuilder
block|{
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|order
specifier|private
name|SortOrder
name|order
decl_stmt|;
DECL|field|missing
specifier|private
name|Object
name|missing
decl_stmt|;
comment|/**      * Constructs a new sort based on a document field.      *      * @param fieldName The field name.      */
DECL|method|FieldSortBuilder
specifier|public
name|FieldSortBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
comment|/**      * The order of sorting. Defaults to {@link SortOrder#ASC}.      */
DECL|method|order
specifier|public
name|FieldSortBuilder
name|order
parameter_list|(
name|SortOrder
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the value when a field is missing in a doc. Can also be set to<tt>_last</tt> or      *<tt>_first</tt> to sort missing last or first respectively.      */
DECL|method|missing
specifier|public
name|FieldSortBuilder
name|missing
parameter_list|(
name|Object
name|missing
parameter_list|)
block|{
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
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
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|order
operator|==
name|SortOrder
operator|.
name|DESC
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"reverse"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|missing
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"missing"
argument_list|,
name|missing
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

