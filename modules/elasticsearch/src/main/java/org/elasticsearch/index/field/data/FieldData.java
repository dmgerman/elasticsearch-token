begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.field.data
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|data
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

begin_comment
comment|// General TODOs on FieldData
end_comment

begin_comment
comment|// TODO Optimize the order (both int[] and int[][] when they are sparse, create an Order abstraction)
end_comment

begin_class
DECL|class|FieldData
specifier|public
specifier|abstract
class|class
name|FieldData
parameter_list|<
name|Doc
extends|extends
name|DocFieldData
parameter_list|>
block|{
DECL|field|cachedDocFieldData
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Doc
argument_list|>
argument_list|>
name|cachedDocFieldData
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Doc
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
name|Doc
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
name|Doc
argument_list|>
argument_list|(
name|createFieldData
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|method|FieldData
specifier|protected
name|FieldData
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
comment|/**      * The field name of this field data.      */
DECL|method|fieldName
specifier|public
specifier|final
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
DECL|method|docFieldData
specifier|public
name|Doc
name|docFieldData
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|Doc
name|docFieldData
init|=
name|cachedDocFieldData
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|docFieldData
operator|.
name|setDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
return|return
name|docFieldData
return|;
block|}
DECL|method|createFieldData
specifier|protected
specifier|abstract
name|Doc
name|createFieldData
parameter_list|()
function_decl|;
comment|/**      * Is the field data a multi valued one (has multiple values / terms per document id) or not.      */
DECL|method|multiValued
specifier|public
specifier|abstract
name|boolean
name|multiValued
parameter_list|()
function_decl|;
comment|/**      * Is there a value associated with this document id.      */
DECL|method|hasValue
specifier|public
specifier|abstract
name|boolean
name|hasValue
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|stringValue
specifier|public
specifier|abstract
name|String
name|stringValue
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|forEachValue
specifier|public
specifier|abstract
name|void
name|forEachValue
parameter_list|(
name|StringValueProc
name|proc
parameter_list|)
function_decl|;
DECL|interface|StringValueProc
specifier|public
specifier|static
interface|interface
name|StringValueProc
block|{
DECL|method|onValue
name|void
name|onValue
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
block|}
DECL|method|forEachValueInDoc
specifier|public
specifier|abstract
name|void
name|forEachValueInDoc
parameter_list|(
name|int
name|docId
parameter_list|,
name|StringValueInDocProc
name|proc
parameter_list|)
function_decl|;
DECL|interface|StringValueInDocProc
specifier|public
specifier|static
interface|interface
name|StringValueInDocProc
block|{
DECL|method|onValue
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|String
name|value
parameter_list|)
function_decl|;
block|}
comment|/**      * The type of this field data.      */
DECL|method|type
specifier|public
specifier|abstract
name|FieldDataType
name|type
parameter_list|()
function_decl|;
DECL|method|load
specifier|public
specifier|static
name|FieldData
name|load
parameter_list|(
name|FieldDataType
name|type
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|type
operator|.
name|load
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

